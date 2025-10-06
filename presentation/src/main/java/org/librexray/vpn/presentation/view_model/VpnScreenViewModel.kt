package org.librexray.vpn.presentation.view_model

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.interfaces.interactor.ConnectionInteractor
import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.domain.models.ImportResult
import org.librexray.vpn.domain.state.ServiceState
import org.librexray.vpn.presentation.mapper.toServerItemModel
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.model.ServerItemModel
import org.librexray.vpn.presentation.state.VpnScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librexray.vpn.domain.interfaces.interactor.SettingsInteractor
import org.librexray.vpn.presentation.di.IoDispatcher
import org.librexray.vpn.presentation.state.VpnScreenError
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * State holder for the VPN screen.
 *
 * Responsibilities:
 * - Exposes immutable [state] and processes [VpnScreenIntent] via [onIntent].
 * - Starts/stops VPN connection and performs lightweight persistence.
 * - Observes service state and downstream speed to keep UI in sync.
 *
 * Threading:
 * - All I/O and long-running work is dispatched on [io]; Main thread is never blocked.
 */
@HiltViewModel
class VpnScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor,
    private val connectionInteractor: ConnectionInteractor,
    private val settingsInteractor: SettingsInteractor,
    stateRepository: ServiceStateRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow(VpnScreenState())
    val state: StateFlow<VpnScreenState> = _state.asStateFlow()

    /**
     * Hot stream with the current VPN service state.
     */
    val serviceState: StateFlow<ServiceState> =
        stateRepository.serviceState.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            ServiceState.Idle
        )

    init {
        viewModelScope.launch(io) {
            updateServerList(configInteractor.getServerList())
            getSelectedServer()
        }
        // Service connectivity in UI state.
        viewModelScope.launch {
            serviceState.collect { serviceState ->
                _state.update {
                    it.copy(
                        isRunning = serviceState == ServiceState.Connected,
                        connectionSpeed = if (serviceState == ServiceState.Connected) it.connectionSpeed else null
                    )
                }
            }
        }
        viewModelScope.launch {
            connectionInteractor.observeSpeed().collect { speed ->
                _state.update { it.copy(connectionSpeed = speed) }
            }
        }
        // Gate for POST_NOTIFICATIONS on API 33+ only.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _state.update {
                it.copy(wasNotificationPermissionAsked = settingsInteractor.wasNotificationAsked())
            }
        }
    }

    /**
     * Entry point for UI events from the VPN screen.
     */
    fun onIntent(intent: VpnScreenIntent) {
        when (intent) {
            VpnScreenIntent.ImportConfigFromClipboard -> importConfigFromClipboard()
            VpnScreenIntent.TestConnection -> testConnection()
            VpnScreenIntent.ToggleConnection -> toggleConnection()
            is VpnScreenIntent.DeleteItem -> deleteItem(intent.id)
            VpnScreenIntent.RefreshItemList -> refreshItemList()
            is VpnScreenIntent.SetSelectedServer -> setSelectedServer(intent.id)
            VpnScreenIntent.ConsumeError -> consumeError()
            VpnScreenIntent.MarkNotificationAsked -> markNotificationAsked()
        }
    }

    /**
     * Toggle current connection state.
     * Safe to call repeatedly; executes on [io].
     */
    private fun toggleConnection() {
        viewModelScope.launch(io) {
            if (state.value.isRunning) stopConnection() else startConnection()
        }
    }

    /**
     * Deletes a server config and updates UI optimistically.
     * Rolls back the list on failure and emits [VpnScreenError.DeleteConfigError].
     */
    private fun deleteItem(guid: String) {
        val before = state.value.serverItemList
        val after = before.filterNot { it.guid == guid }

        viewModelScope.launch(io) {
            runCatching { configInteractor.deleteItem(guid) }
                .onSuccess {
                    _state.update { it.copy(serverItemList = after) }
                    if (guid == state.value.selectedServerId) {
                        val newSelected = after.firstOrNull()?.guid
                        _state.update { it.copy(selectedServerId = newSelected) }
                        if (newSelected != null) setSelectedServer(newSelected)
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _state.update {
                        it.copy(
                            serverItemList = before,
                            error = VpnScreenError.DeleteConfigError
                        )
                    }
                }
        }
    }

    /**
     * Runs connectivity test and stores measured delay on success.
     * Emits [VpnScreenError.TestConnectionError] on failure.
     */
    private fun testConnection() {
        viewModelScope.launch(io) {
            runCatching {
                val delay = connectionInteractor.testConnection()
                _state.update { it.copy(delay = delay) }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                _state.update { it.copy(error = VpnScreenError.TestConnectionError) }
            }
        }
    }

    /**
     * Tries to import a server configuration from the clipboard.
     * - Empty → [VpnScreenError.EmptyConfigError]
     * - Error → [VpnScreenError.ImportConfigError]
     * - Success → refreshes server list
     */
    private fun importConfigFromClipboard() {
        viewModelScope.launch(io) {
            when (configInteractor.importClipboardConfig()) {
                ImportResult.Empty -> _state.update {
                    it.copy(error = VpnScreenError.EmptyConfigError)
                }

                ImportResult.Error -> _state.update {
                    it.copy(error = VpnScreenError.ImportConfigError)
                }

                ImportResult.Success -> updateServerList(configInteractor.getServerList())
            }
        }
    }

    /** Forces a full refresh of the server list. */
    private fun refreshItemList() {
        viewModelScope.launch(io) {
            updateServerList(configInteractor.getServerList())
        }
    }

    /**
     * Rebuilds [VpnScreenState.serverItemList] from stored GUIDs.
     * Ensures launch spinner is hidden even on failure.
     * On exception emits [VpnScreenError.UpdateServerListError].
     */
    private suspend fun updateServerList(serverList: List<String>) {
        getSelectedServer()
        try {
            val items: List<ServerItemModel> = serverList.mapNotNull { guid ->
                configInteractor.getServerConfig(guid)?.toServerItemModel(guid)
            }
            _state.update { it.copy(isLaunchLoading = false, serverItemList = items) }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            _state.update {
                it.copy(
                    isLaunchLoading = false,
                    error = VpnScreenError.UpdateServerListError
                )
            }
        }
    }


    /** Reads the currently selected server id and reflects it in UI state. */
    private suspend fun getSelectedServer() {
        val selectedServer = configInteractor.getSelectedServer()
        _state.update { it.copy(selectedServerId = selectedServer) }
    }

    /**
     * Persists the selected server and updates UI.
     * Emits [VpnScreenError.SelectServerError] on failure.
     */
    private fun setSelectedServer(serverId: String) {
        viewModelScope.launch(io) {
            runCatching {
                configInteractor.setSelectedServer(serverId)
                _state.update { it.copy(selectedServerId = serverId) }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                _state.update { it.copy(error = VpnScreenError.SelectServerError) }
            }
        }
    }

    /** Starts the VPN connection; resets last delay; emits [VpnScreenError.StartError] on failure. */
    private suspend fun startConnection() {
        _state.update { it.copy(delay = null) }
        runCatching {
            connectionInteractor.startConnection()
        }.onFailure { e ->
            if (e is CancellationException) throw e
            _state.update { it.copy(error = VpnScreenError.StartError) }
        }
    }

    /** Stops the VPN connection; emits [VpnScreenError.StopError] on failure. */
    private suspend fun stopConnection() {
        runCatching {
            connectionInteractor.stopConnection()
        }.onFailure { e ->
            if (e is CancellationException) throw e
            _state.update { it.copy(error = VpnScreenError.StopError) }
        }
    }

    /** Consumes the current error so it is not rendered again. */
    private fun consumeError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Marks that POST_NOTIFICATIONS prompt was shown once (API 33+).
     * Updates both persistent storage and in-memory state.
     */
    private fun markNotificationAsked() {
        viewModelScope.launch(io) {
            settingsInteractor.markNotificationAsked()
        }
        _state.update { it.copy(wasNotificationPermissionAsked = true) }
    }
}
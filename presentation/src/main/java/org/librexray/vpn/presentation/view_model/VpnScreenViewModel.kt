package org.librexray.vpn.presentation.view_model

import  androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.interfaces.interactor.ConnectionInteractor
import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.domain.models.ImportResult
import org.librexray.vpn.domain.state.ServiceState
import org.librexray.vpn.presentation.formatter.toServerItemModel
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.models.ServerItemModel
import org.librexray.vpn.presentation.state.VpnScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class VpnScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor,
    private val connectionInteractor: ConnectionInteractor,
    stateRepository: ServiceStateRepository
) : ViewModel() {
    private val _state = MutableStateFlow(VpnScreenState())
    val state: StateFlow<VpnScreenState> = _state.asStateFlow()

    val serviceState: StateFlow<ServiceState> =
        stateRepository.serviceState.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            ServiceState.Idle
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateServerList(configInteractor.getServerList())
            getSelectedServer()
        }
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
    }

    fun onIntent(intent: VpnScreenIntent) {
        when (intent) {
            VpnScreenIntent.ImportConfigFromClipboard -> importConfigFromClipboard()
            VpnScreenIntent.RestartConnection -> restartConnection()
            VpnScreenIntent.TestConnection -> testConnection()
            VpnScreenIntent.ToggleConnection -> toggleConnection()
            is VpnScreenIntent.DeleteItem -> deleteItem(intent.id)
            VpnScreenIntent.RefreshItemList -> refreshItemList()
            is VpnScreenIntent.SetSelectedServer -> setSelectedServer(intent.id)
        }
    }

    private fun toggleConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.isRunning) stopConnection() else startConnection()
        }
    }


    private fun deleteItem(guid: String) {
        val before = state.value.serverItemList
        val after = before.filterNot { it.guid == guid }

        _state.update { it.copy(serverItemList = after) }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { configInteractor.deleteItem(guid) }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            serverItemList = before,
                            error = e.message ?: "Delete failed",
                            selectedServerId = guid
                        )
                    }
                }
        }
        if (guid == state.value.selectedServerId) {
            val newSelected = after.firstOrNull()?.guid ?: return
            setSelectedServer(newSelected)
        }
    }

    private fun testConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            val delay = connectionInteractor.testConnection()
            _state.update { it.copy(delay = delay) }
        }
    }

    private fun restartConnection() {
        if (state.value.isRunning) {
            connectionInteractor.restartConnection()
        } else {
            _state.update { it.copy(error = "Connection is not running") }
        }
    }

    private fun importConfigFromClipboard() {
        viewModelScope.launch(Dispatchers.IO) {
            when (configInteractor.importClipboardConfig()) {
                ImportResult.Empty -> _state.update {
                    it.copy(
                        error = "Empty config"
                    )
                }

                ImportResult.Error -> _state.update {
                    it.copy(
                        error = "Config imported error"
                    )
                }

                ImportResult.Success -> updateServerList(configInteractor.getServerList())
            }
        }
    }

    private fun refreshItemList() {
        viewModelScope.launch(Dispatchers.IO) {
            updateServerList(configInteractor.getServerList())
        }
    }

    private suspend fun updateServerList(serverList: List<String>) {
        getSelectedServer()
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val items: List<ServerItemModel> = serverList.mapNotNull { guid ->
                configInteractor.getServerConfig(guid)?.toServerItemModel(guid)
            }
            _state.update { it.copy(isLoading = false, serverItemList = items) }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.message ?: "Unknown error") }
        }
    }

    private suspend fun getSelectedServer() {
        val selectedServer = configInteractor.getSelectedServer()
        _state.update { it.copy(selectedServerId = selectedServer) }
    }

    private fun setSelectedServer(serverId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            configInteractor.setSelectedServer(serverId)
            _state.update { it.copy(selectedServerId = serverId) }
        }
    }

    private suspend fun startConnection() {
        connectionInteractor.startConnection()
    }

    private suspend fun stopConnection() {
        connectionInteractor.stopConnection()
    }
}
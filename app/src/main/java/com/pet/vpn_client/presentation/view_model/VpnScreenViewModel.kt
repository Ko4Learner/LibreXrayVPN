package com.pet.vpn_client.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.state.ServiceState
import com.pet.vpn_client.presentation.intent.VpnScreenIntent
import com.pet.vpn_client.presentation.models.ServerItemModel
import com.pet.vpn_client.presentation.state.VpnScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor,
    private val connectionInteractor: ConnectionInteractor,
    private val settingsInteractor: SettingsInteractor,
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
        viewModelScope.launch {
            updateServerList(configInteractor.getServerList())
            _state.update { it.copy(isVpnMode = settingsInteractor.getMode() == Constants.VPN) }
            serviceState.collect { serviceState ->
                _state.update { it.copy(isRunning = serviceState == ServiceState.Connected) }
            }
        }
    }

    fun onIntent(intent: VpnScreenIntent) {
        when (intent) {
            VpnScreenIntent.ImportConfigFromClipboard -> importConfigFromClipboard()
            VpnScreenIntent.RestartConnection -> restartConnection()
            VpnScreenIntent.SwitchVpnProxy -> switchVpnProxy()
            VpnScreenIntent.TestConnection -> testConnection()
            VpnScreenIntent.ToggleConnection -> toggleConnection()
            is VpnScreenIntent.DeleteItem -> deleteItem(intent.id)
            VpnScreenIntent.RefreshItemList -> refreshItemList()
        }
    }

    private fun toggleConnection() {
        viewModelScope.launch {
            if (state.value.isRunning) {
                stopConnection()
            } else {
                startConnection()
            }
        }
    }


    private fun deleteItem(id: String) {
        _state.update { it ->
            it.copy(
                isLoading = true,
                serverItemList = it.serverItemList - it.serverItemList.first { it.guid == id }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            configInteractor.deleteItem(id)
        }
        _state.update { it.copy(isLoading = false) }
    }

    private fun switchVpnProxy() {
        viewModelScope.launch(Dispatchers.Default) {
            val isRunning = state.value.isRunning
            if (state.value.isVpnMode) {
                _state.update { it.copy(isVpnMode = false) }
                if (isRunning) connectionInteractor.stopConnection()
                settingsInteractor.setProxyMode()
            } else {
                _state.update { it.copy(isVpnMode = true) }
                if (isRunning) connectionInteractor.stopConnection()
                settingsInteractor.setVpnMode()
            }
            if (isRunning) {
                serviceState
                    .filter { it is ServiceState.Idle || it is ServiceState.Stopped }
                    .first()
                connectionInteractor.startConnection()
            }
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
            if (configInteractor.importClipboardConfig() >= 0) {
                updateServerList(configInteractor.getServerList())
            } else {
                _state.update {
                    it.copy(
                        serverItemList = listOf(),
                        error = "Config imported error"
                    )
                }
            }
        }
    }

    private fun refreshItemList() {
        viewModelScope.launch(Dispatchers.IO) {
            updateServerList(configInteractor.getServerList())
        }
    }

    private suspend fun updateServerList(serverList: List<String>) {
        _state.update { it.copy(isLoading = true, serverItemList = mutableListOf(), error = null) }
        serverList.forEach { guid ->
            val profile = configInteractor.getServerConfig(guid)
            if (profile != null) {
                _state.update {
                    it.copy(
                        isLoading = true,
                        serverItemList = it.serverItemList + ServerItemModel(guid, profile)
                    )
                }
            }
        }
        _state.update { it.copy(isLoading = false) }
    }

    private suspend fun startConnection() {
        connectionInteractor.startConnection()
    }

    private suspend fun stopConnection() {
        connectionInteractor.stopConnection()
    }
}
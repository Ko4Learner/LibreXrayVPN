package com.pet.vpn_client.presentation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import com.pet.vpn_client.presentation.intent.VpnScreenIntent
import com.pet.vpn_client.presentation.models.ServerItemModel
import com.pet.vpn_client.presentation.state.VpnScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor,
    private val connectionInteractor: ConnectionInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(VpnScreenState())
    val state: StateFlow<VpnScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            updateServerList(configInteractor.getServerList())
            _state.update { it.copy(isVpnMode = settingsInteractor.getMode() == Constants.VPN) }
        }
    }

    fun onIntent(intent: VpnScreenIntent) {
        when (intent) {
            VpnScreenIntent.ImportConfigFromClipboard -> importConfigFromClipboard()
            VpnScreenIntent.RestartConnection -> restartConnection()
            VpnScreenIntent.SwitchVpnProxy -> switchVpnProxy()
            VpnScreenIntent.TestConnection -> testConnection()
            VpnScreenIntent.ToggleVpnProxy -> toggleVpnProxy()
            is VpnScreenIntent.DeleteItem -> deleteItem(intent.id)
            VpnScreenIntent.RefreshItemList -> refreshItemList()
        }
    }

    private fun toggleVpnProxy() {
        viewModelScope.launch {
            if (state.value.isRunning) {
                stopConnection()
            } else {
                startConnection()
            }
        }
    }

    private fun deleteItem(id: String) {
        _state.update {
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
        //TODO добавить перезапуск Vpn/Proxy с нужным модом
        viewModelScope.launch {
            if (state.value.isVpnMode) {
                settingsInteractor.setProxyMode()
                _state.update { it.copy(isVpnMode = false) }
            } else {
                settingsInteractor.setVpnMode()
                _state.update { it.copy(isVpnMode = true) }
            }
        }
    }

    private fun testConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(Constants.TAG, "Test connection")
            val delay = connectionInteractor.testConnection()
            _state.update { it.copy(delay = delay) }
        }
    }

    private fun restartConnection() {
        if (state.value.isRunning) {
            viewModelScope.launch {
                stopConnection()
                delay(500)
                startConnection()
            }
        } else {
            _state.update { it.copy(error = "Connection is not running") }
        }
    }

    private fun importConfigFromClipboard() {
        viewModelScope.launch(Dispatchers.IO) {
            //TODO добавить проверку на -1 и 0
            if (configInteractor.importClipboardConfig() >= 0) {
                updateServerList(configInteractor.getServerList())
            } else {
                Log.d(Constants.TAG, "Config imported error")
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
        if (connectionInteractor.startConnection()) {
            _state.update { it.copy(isRunning = true) }
            Log.d(Constants.TAG, "Connection")
        } else {
            _state.update { it.copy(isRunning = false, error = "Connection error") }
            Log.d(Constants.TAG, "Connection error")
        }
    }

    private suspend fun stopConnection() {
        connectionInteractor.stopConnection()
        _state.update { it.copy(isRunning = false) }
        Log.d(Constants.TAG, "Stop connection")
    }
}
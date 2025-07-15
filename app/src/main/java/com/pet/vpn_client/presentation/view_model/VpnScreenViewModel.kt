package com.pet.vpn_client.presentation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.presentation.intent.VpnScreenIntent
import com.pet.vpn_client.presentation.models.ServerItemModel
import com.pet.vpn_client.presentation.state.VpnScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor,
    private val connectionInteractor: ConnectionInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(VpnScreenState())
    val state: StateFlow<VpnScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            updateServerList(configInteractor.getServerList())
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
        //TODO: Implement VPN/Proxy switching logic
    }

    private fun testConnection() {
        //TODO: Implement testing connection
    }

    private fun restartConnection() {
        //TODO: Implement restarting connection
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
        val selectedConfig = configInteractor.getSelectedServer()
        Log.d(Constants.TAG, "Selected config: $selectedConfig")
        //TODO разрешения
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
    }
}
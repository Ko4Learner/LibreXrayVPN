package com.pet.vpn_client.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnScreenViewModel @Inject constructor(val configInteractor: ConfigInteractor) : ViewModel() {
    fun toggleVpnProxy() {
        //TODO: Implement VPN/Proxy toggle logic
    }

    fun switchVpnProxy() {
        //TODO: Implement VPN/Proxy switching logic
    }

    fun getSubscriptions() {
        //TODO: Implement getting subscriptions
    }

    fun testConnection() {
        //TODO: Implement testing connection
    }

    fun restartConnection() {
        //TODO: Implement restarting connection
    }

    fun importConfigFromClipboard() {
        viewModelScope.launch {
            configInteractor.importClipboardConfig()
        }
    }
}
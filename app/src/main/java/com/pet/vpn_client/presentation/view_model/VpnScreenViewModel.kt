package com.pet.vpn_client.presentation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnScreenViewModel @Inject constructor(private val configInteractor: ConfigInteractor) : ViewModel() {
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
            //TODO добавить проверку на -1 и 0
            val count = configInteractor.importClipboardConfig()
            if (count >= 0) {
                Log.d(Constants.TAG, configInteractor.getServerList().toString())
                updateServerList(configInteractor.getServerList())
            } else {
                Log.d(Constants.TAG, "Config imported error")
                //TODO: Handle error
            }
        }
    }

    private fun updateServerList(serverList: List<String>) {

    }
}
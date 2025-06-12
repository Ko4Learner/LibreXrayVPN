package com.pet.vpn_client.presentation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.presentation.models.ServerItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnScreenViewModel @Inject constructor(private val configInteractor: ConfigInteractor) :
    ViewModel() {

    private var serverList: List<String> = emptyList()
    //TODO переделать в аналог liveData
    val serverItemList: MutableList<ServerItemModel> = mutableListOf()

    init {
        viewModelScope.launch {
            serverList = configInteractor.getServerList()
            updateServerList(serverList)
        }
    }

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
            if (configInteractor.importClipboardConfig() >= 0) {
                serverList = configInteractor.getServerList()
                updateServerList(serverList)
            } else {
                Log.d(Constants.TAG, "Config imported error")
                //TODO: Handle error
            }
        }
    }

    private suspend fun updateServerList(serverList: List<String>) {
        serverItemList.clear()
        serverList.forEach { guid ->
            val profile = configInteractor.getServerConfig(guid)
            if (profile != null) {
                serverItemList.add(ServerItemModel(guid, profile))
            }
        }

    }
}
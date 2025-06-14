package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import javax.inject.Inject

class ConnectionInteractorImpl @Inject constructor(val serviceManager: ServiceManager) :
    ConnectionInteractor {
    override suspend fun startConnection(): Boolean {
        return serviceManager.startServiceFromToggle()
    }

    override suspend fun stopConnection() {
        serviceManager.stopService()
    }

    override suspend fun restartConnection(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun testConnection(): Boolean {
        TODO("Not yet implemented")
    }
}
package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(private val keyStorage: KeyValueStorage) :
    SettingsInteractor {
    override suspend fun setProxyMode() {
        keyStorage.setProxyMode()
    }

    override suspend fun setVpnMode() {
        keyStorage.setVpnMode()
    }

    override suspend fun getMode(): String {
        return keyStorage.getMode()
    }
}
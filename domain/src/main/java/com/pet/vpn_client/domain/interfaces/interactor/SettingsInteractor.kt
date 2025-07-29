package com.pet.vpn_client.domain.interfaces.interactor

interface SettingsInteractor {
    suspend fun setProxyMode()
    suspend fun setVpnMode()
    suspend fun getMode(): String
}
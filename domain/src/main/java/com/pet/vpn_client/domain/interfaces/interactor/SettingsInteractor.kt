package com.pet.vpn_client.domain.interfaces.interactor

import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface SettingsInteractor {
    suspend fun setProxyMode()
    suspend fun setVpnMode()
    suspend fun getMode(): String
    suspend fun setLocale(localeTag: String)
    fun observeLocale(): Flow<Locale>
}
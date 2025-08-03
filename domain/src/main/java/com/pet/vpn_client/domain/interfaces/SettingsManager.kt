package com.pet.vpn_client.domain.interfaces

import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface SettingsManager {
    fun observeLocale(): Flow<Locale>
    fun getLocale(): Locale
    suspend fun setLocale(localeTag: String)
}
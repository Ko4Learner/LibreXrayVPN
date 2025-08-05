package com.pet.vpn_client.domain.interfaces.repository

import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface SettingsRepository {
    fun observeLocale(): Flow<Locale>
    fun getLocale(): Locale
    suspend fun setLocale(localeTag: String)
}
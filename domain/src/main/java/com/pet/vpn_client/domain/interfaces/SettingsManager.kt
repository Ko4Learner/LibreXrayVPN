package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.ConfigProfileItem
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface SettingsManager {
    fun getHttpPort(): Int
    fun getSocksPort(): Int
    fun getRemoteDnsServers(): List<String>
    fun getDomesticDnsServers(): List<String>
    fun getServerViaRemarks(remarks: String?): ConfigProfileItem?
    fun getDelayTestUrl(second: Boolean = false): String
    fun observeLocale(): Flow<Locale>
    fun getLocale(): Locale
    suspend fun setLocale(localeTag: String)
}
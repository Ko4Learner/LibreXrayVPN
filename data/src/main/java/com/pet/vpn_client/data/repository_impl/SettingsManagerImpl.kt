package com.pet.vpn_client.data.repository_impl

import com.google.gson.Gson
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import javax.inject.Inject

class SettingsManagerImpl @Inject constructor(val storage: KeyValueStorage, val gson: Gson) :
    SettingsManager {

    private val _localeFlow = MutableStateFlow(getLocale())
    override fun observeLocale(): Flow<Locale> = _localeFlow

    override suspend fun setLocale(localeTag: String) {
        storage.encodeSettings(Constants.PREF_LANGUAGE, localeTag)
        _localeFlow.value = getLocale()
    }

    override fun getLocale(): Locale {
        val tag = storage.decodeSettingsString(Constants.PREF_LANGUAGE)
        return when {
            tag.isNullOrEmpty() || tag == Constants.AUTO_LOCALE_TAG -> Locale.getDefault()
            else -> Locale.forLanguageTag(tag)
        }
    }

    override fun getServerViaRemarks(remarks: String?): ConfigProfileItem? {
        if (remarks == null) {
            return null
        }
        val serverList = storage.decodeServerList()
        for (guid in serverList) {
            val profile = storage.decodeServerConfig(guid)
            if (profile != null && profile.remarks == remarks) {
                return profile
            }
        }
        return null
    }

    override fun getSocksPort(): Int {
        return 10808
    }

    override fun getHttpPort(): Int {
        return getSocksPort() + 0
    }

    override fun getDomesticDnsServers(): List<String> {
        val domesticDns = Constants.DNS_DIRECT
        val ret = domesticDns.split(",")
            .filter { Utils.isPureIpAddress(it) || Utils.isCoreDNSAddress(it) }
        if (ret.isEmpty()) {
            return listOf(Constants.DNS_DIRECT)
        }
        return ret
    }

    override fun getRemoteDnsServers(): List<String> {
        val remoteDns = Constants.DNS_PROXY
        val ret =
            remoteDns.split(",").filter { Utils.isPureIpAddress(it) || Utils.isCoreDNSAddress(it) }
        if (ret.isEmpty()) {
            return listOf(Constants.DNS_PROXY)
        }
        return ret
    }

    override fun getDelayTestUrl(second: Boolean): String {
        return if (second) {
            Constants.DELAY_TEST_URL2
        } else {
            Constants.DELAY_TEST_URL
        }
    }
}
package com.pet.vpn_client.data

import android.content.Context
import android.content.res.AssetManager
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.Language
import com.pet.vpn_client.domain.models.RoutingType
import com.pet.vpn_client.domain.models.RulesetItem
import com.pet.vpn_client.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import java.util.Locale
import javax.inject.Inject

class SettingsManagerImpl @Inject constructor(val storage: KeyValueStorage, val gson: Gson): SettingsManager {

    fun initRoutingRulesets(context: Context) {
        val exist = storage.decodeRoutingRulesets()
        if (exist.isNullOrEmpty()) {
            val rulesetList = getPresetRoutingRulesets(context)
            storage.encodeRoutingRulesets(rulesetList)
        }
    }

    private fun getPresetRoutingRulesets(
        context: Context,
        index: Int = 0
    ): MutableList<RulesetItem>? {
        val fileName = RoutingType.fromIndex(index).fileName
        val assets = Utils.readTextFromAssets(context, fileName)
        if (TextUtils.isEmpty(assets)) {
            return null
        }

        return gson.fromJson(assets, Array<RulesetItem>::class.java).toMutableList()
    }

    fun resetRoutingRulesetsFromPresets(context: Context, index: Int) {
        val rulesetList = getPresetRoutingRulesets(context, index) ?: return
        resetRoutingRulesetsCommon(rulesetList)
    }

    fun resetRoutingRulesets(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }

        try {
            val rulesetList = gson.fromJson(content, Array<RulesetItem>::class.java).toMutableList()
            if (rulesetList.isNullOrEmpty()) {
                return false
            }

            resetRoutingRulesetsCommon(rulesetList)
            return true
        } catch (e: Exception) {
            Log.e(Constants.ANG_PACKAGE, "Failed to reset routing rulesets", e)
            return false
        }
    }

    private fun resetRoutingRulesetsCommon(rulesetList: MutableList<RulesetItem>) {
        val rulesetNew: MutableList<RulesetItem> = mutableListOf()
        storage.decodeRoutingRulesets()?.forEach { key ->
            if (key.locked == true) {
                rulesetNew.add(key)
            }
        }

        rulesetNew.addAll(rulesetList)
        storage.encodeRoutingRulesets(rulesetNew)
    }

    fun getRoutingRuleset(index: Int): RulesetItem? {
        if (index < 0) return null

        val rulesetList = storage.decodeRoutingRulesets()
        if (rulesetList.isNullOrEmpty()) return null

        return rulesetList[index]
    }

    fun saveRoutingRuleset(index: Int, ruleset: RulesetItem?) {
        if (ruleset == null) return

        var rulesetList = storage.decodeRoutingRulesets()
        if (rulesetList.isNullOrEmpty()) {
            rulesetList = mutableListOf()
        }

        if (index < 0 || index >= rulesetList.count()) {
            rulesetList.add(0, ruleset)
        } else {
            rulesetList[index] = ruleset
        }
        storage.encodeRoutingRulesets(rulesetList)
    }

    fun removeRoutingRuleset(index: Int) {
        if (index < 0) return

        val rulesetList = storage.decodeRoutingRulesets()
        if (rulesetList.isNullOrEmpty()) return

        rulesetList.removeAt(index)
        storage.encodeRoutingRulesets(rulesetList)
    }

    // реализация разделенного тунелирования
    fun routingRulesetsBypassLan(): Boolean {
        val vpnBypassLan = storage.decodeSettingsString(Constants.PREF_VPN_BYPASS_LAN) ?: "0"
        if (vpnBypassLan == "1") {
            return true
        } else if (vpnBypassLan == "2") {
            return false
        }

        val guid = storage.getSelectServer() ?: return false

        val rulesetItems = storage.decodeRoutingRulesets()
        val exist =
            rulesetItems?.filter { it.enabled && it.outboundTag == Constants.TAG_DIRECT }?.any {
                it.domain?.contains(Constants.GEOSITE_PRIVATE) == true || it.ip?.contains(Constants.GEOIP_PRIVATE) == true
            }
        return exist == true
    }

    fun swapRoutingRuleset(fromPosition: Int, toPosition: Int) {
        val rulesetList = storage.decodeRoutingRulesets()
        if (rulesetList.isNullOrEmpty()) return

        Collections.swap(rulesetList, fromPosition, toPosition)
        storage.encodeRoutingRulesets(rulesetList)
    }

    fun swapSubscriptions(fromPosition: Int, toPosition: Int) {
        val subsList = storage.decodeSubsList()
        if (subsList.isNullOrEmpty()) return

        Collections.swap(subsList, fromPosition, toPosition)
        storage.encodeSubsList(subsList)
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
        return Utils.parseInt(
            storage.decodeSettingsString(Constants.PREF_SOCKS_PORT),
            Constants.PORT_SOCKS.toInt()
        )
    }

    override fun getHttpPort(): Int {
        return getSocksPort() + 0
        //return getSocksPort() + if (Utils.isXray()) 0 else 1
    }

    fun initAssets(context: Context, assets: AssetManager) {
        val extFolder = Utils.userAssetPath(context)

        try {
            val geo = arrayOf("geosite.dat", "geoip.dat")
            assets.list("")
                ?.filter { geo.contains(it) }
                ?.filter { !File(extFolder, it).exists() }
                ?.forEach {
                    val target = File(extFolder, it)
                    assets.open(it).use { input ->
                        FileOutputStream(target).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.i(Constants.TAG, "Copied from apk assets folder to ${target.absolutePath}")
                }
        } catch (e: Exception) {
            Log.e(Constants.ANG_PACKAGE, "asset copy failed", e)
        }
    }

    override fun getDomesticDnsServers(): List<String> {
        val domesticDns =
            storage.decodeSettingsString(Constants.PREF_DOMESTIC_DNS) ?: Constants.DNS_DIRECT
        val ret = domesticDns.split(",")
            .filter { Utils.isPureIpAddress(it) || Utils.isCoreDNSAddress(it) }
        if (ret.isEmpty()) {
            return listOf(Constants.DNS_DIRECT)
        }
        return ret
    }

    override fun getRemoteDnsServers(): List<String> {
        val remoteDns =
            storage.decodeSettingsString(Constants.PREF_REMOTE_DNS) ?: Constants.DNS_PROXY
        val ret =
            remoteDns.split(",").filter { Utils.isPureIpAddress(it) || Utils.isCoreDNSAddress(it) }
        if (ret.isEmpty()) {
            return listOf(Constants.DNS_PROXY)
        }
        return ret
    }

    override fun getVpnDnsServers(): List<String> {
        val vpnDns = storage.decodeSettingsString(Constants.PREF_VPN_DNS) ?: Constants.DNS_VPN
        return vpnDns.split(",").filter { Utils.isPureIpAddress(it) }
    }

    override fun getDelayTestUrl(second: Boolean): String {
        return if (second) {
            Constants.DELAY_TEST_URL2
        } else {
            storage.decodeSettingsString(Constants.PREF_DELAY_TEST_URL)
                ?: Constants.DELAY_TEST_URL
        }
    }

    fun getLocale(): Locale {
        val langCode =
            storage.decodeSettingsString(Constants.PREF_LANGUAGE) ?: Language.AUTO.code
        val language = Language.fromCode(langCode)

        return when (language) {
            Language.AUTO -> Utils.getSysLocale()
            Language.ENGLISH -> Locale.ENGLISH
            Language.CHINA -> Locale.CHINA
            Language.TRADITIONAL_CHINESE -> Locale.TRADITIONAL_CHINESE
            Language.VIETNAMESE -> Locale("vi")
            Language.RUSSIAN -> Locale("ru")
            Language.PERSIAN -> Locale("fa")
            Language.ARABIC -> Locale("ar")
            Language.BANGLA -> Locale("bn")
            Language.BAKHTIARI -> Locale("bqi", "IR")
        }
    }

    fun setNightMode() {
//        when (storage.decodeSettingsString(Constants.PREF_UI_MODE_NIGHT, "0")) {
//            "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//            "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
    }
}
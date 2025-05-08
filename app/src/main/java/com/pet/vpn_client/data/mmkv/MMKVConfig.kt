package com.pet.vpn_client.data.mmkv

import com.google.gson.Gson
import com.pet.vpn_client.data.dto.AssetUrlItem
import com.pet.vpn_client.data.dto.ConfigProfileItem
import com.pet.vpn_client.data.dto.RulesetItem
import com.pet.vpn_client.data.dto.ServerTestDelayInfo
import com.pet.vpn_client.data.dto.SubscriptionItem
import com.tencent.mmkv.MMKV
import java.util.UUID
import javax.inject.Inject

class MMKVConfig @Inject constructor(private val gson: Gson) {

    companion object {
        private const val ID_MAIN = "MAIN"
        private const val ID_PROFILE_FULL_CONFIG = "PROFILE_FULL_CONFIG"
        private const val ID_SERVER_RAW = "SERVER_RAW"
        private const val ID_SERVER_TEST_DELAY = "SERVER_TEST_DELAY"
        private const val ID_SUB = "SUB"
        private const val ID_ASSET = "ASSET"
        private const val ID_SETTING = "SETTING"
        private const val KEY_SELECTED_SERVER = "SELECTED_SERVER"
        private const val KEY_ANG_CONFIGS = "ANG_CONFIGS"
        private const val KEY_SUB_IDS = "SUB_IDS"
    }

    private val mainStorage by lazy { MMKV.mmkvWithID(ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val profileFullStorage by lazy {
        MMKV.mmkvWithID(
            ID_PROFILE_FULL_CONFIG,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val serverRawStorage by lazy { MMKV.mmkvWithID(ID_SERVER_RAW, MMKV.MULTI_PROCESS_MODE) }
    private val serverTestDelayStorage by lazy {
        MMKV.mmkvWithID(
            ID_SERVER_TEST_DELAY,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val subStorage by lazy { MMKV.mmkvWithID(ID_SUB, MMKV.MULTI_PROCESS_MODE) }
    private val assetStorage by lazy { MMKV.mmkvWithID(ID_ASSET, MMKV.MULTI_PROCESS_MODE) }
    private val settingsStorage by lazy { MMKV.mmkvWithID(ID_SETTING, MMKV.MULTI_PROCESS_MODE) }

    fun getSelectServer(): String? {
        return mainStorage.decodeString(KEY_SELECTED_SERVER)
    }

    fun setSelectServer(guid: String) {
        mainStorage.encode(KEY_SELECTED_SERVER, guid)
    }

    fun encodeServerList(serverList: MutableList<String>) {
        mainStorage.encode(KEY_ANG_CONFIGS, gson.toJson(serverList))
    }

    fun decodeServerList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_ANG_CONFIGS)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            //(json, Array<String>::class.java).toMutableList()
            gson.fromJson(json, mutableListOf<String>()::class.java)
        }
    }

    fun decodeServerConfig(guid: String): ConfigProfileItem? {
        if (guid.isBlank()) {
            return null
        }
        val json = profileFullStorage.decodeString(guid)
        if (json.isNullOrBlank()) {
            return null
        }
        return gson.fromJson(json, ConfigProfileItem::class.java)
    }


    fun encodeServerConfig(guid: String, config: ConfigProfileItem): String {
        val key = guid.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        profileFullStorage.encode(key, gson.toJson(config))
        val serverList = decodeServerList()
        if (!serverList.contains(key)) {
            serverList.add(0, key)
            encodeServerList(serverList)
            if (getSelectServer().isNullOrBlank()) {
                mainStorage.encode(KEY_SELECTED_SERVER, key)
            }
        }
        return key
    }

    fun removeServer(guid: String) {
        if (guid.isBlank()) {
            return
        }
        if (getSelectServer() == guid) {
            mainStorage.remove(KEY_SELECTED_SERVER)
        }
        val serverList = decodeServerList()
        serverList.remove(guid)
        encodeServerList(serverList)
        profileFullStorage.remove(guid)
        serverTestDelayStorage.remove(guid)
    }

    fun removeServerViaSubId(subId: String) {
        if (subId.isBlank()) {
            return
        }
        profileFullStorage.allKeys()?.forEach { key ->
            decodeServerConfig(key)?.let { config ->
                if (config.subscriptionId == subId) {
                    removeServer(key)
                }
            }
        }
    }

    fun decodeServerTestDelayInfo(guid: String): ServerTestDelayInfo? {
        if (guid.isBlank()) {
            return null
        }
        val json = serverTestDelayStorage.decodeString(guid)
        if (json.isNullOrBlank()) {
            return null
        }
        return gson.fromJson(json, ServerTestDelayInfo::class.java)
    }

    fun encodeServerTestDelayInfo(guid: String, delayResult: Long) {
        if (guid.isBlank()) {
            return
        }
        val delay = decodeServerTestDelayInfo(guid) ?: ServerTestDelayInfo()
        delay.testDelayMillis = delayResult
        serverTestDelayStorage.encode(guid, gson.toJson(delay))
    }

    fun clearAllTestDelayResults(keys: List<String>?) {
        keys?.forEach { key ->
            serverTestDelayStorage.encode(key, gson.toJson(ServerTestDelayInfo(0L)))
        }
    }

    fun removeAllServer(): Int {
        val count = profileFullStorage.allKeys()?.count() ?: 0
        mainStorage.clearAll()
        profileFullStorage.clearAll()
        serverTestDelayStorage.clearAll()
        return count
    }

    fun removeInvalidServer(guid: String): Int {
        var count = 0
        if (guid.isNotEmpty()) {
            decodeServerTestDelayInfo(guid)?.let { delay ->
                if (delay.testDelayMillis < 0L) {
                    removeServer(guid)
                    count++
                }
            }
        } else {
            serverTestDelayStorage.allKeys()?.forEach { key ->
                decodeServerTestDelayInfo(key)?.let { delay ->
                    if (delay.testDelayMillis < 0L) {
                        removeServer(key)
                        count++
                    }
                }
            }
        }
        return count
    }

    fun encodeServerRaw(guid: String, config: String) {
        serverRawStorage.encode(guid, config)
    }

    fun decodeServerRaw(guid: String): String? {
        return serverRawStorage.decodeString(guid)
    }

    fun encodeSubsList(subsList: MutableList<String>) {
        mainStorage.encode(KEY_SUB_IDS, gson.toJson(subsList))
    }

    fun decodeSubsList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_SUB_IDS)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            gson.fromJson(json, mutableListOf<String>()::class.java)
        }
    }

    private fun initSubsList() {
        val subsList = decodeSubsList()
        if (subsList.isNotEmpty()) {
            return
        }
        subStorage.allKeys()?.forEach { key ->
            subsList.add(key)
        }
        encodeSubsList(subsList)
    }

    fun decodeSubscriptions(): List<Pair<String, SubscriptionItem>> {
        initSubsList()

        val subscriptions = mutableListOf<Pair<String, SubscriptionItem>>()
        decodeSubsList().forEach { key ->
            val json = subStorage.decodeString(key)
            if (!json.isNullOrBlank()) {
                subscriptions.add(Pair(key, gson.fromJson(json, SubscriptionItem::class.java)))
            }
        }
        return subscriptions
    }

    fun removeSubscription(subId: String) {
        subStorage.remove(subId)
        val subsList = decodeSubsList()
        subsList.remove(subId)
        encodeSubsList(subsList)

        removeServerViaSubId(subId)
    }

    fun encodeSubscription(guid: String, subItem: SubscriptionItem) {
        val key = guid.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        subStorage.encode(key, gson.toJson(subItem))

        val subsList = decodeSubsList()
        if (!subsList.contains(key)) {
            subsList.add(key)
            encodeSubsList(subsList)
        }
    }

    fun decodeSubscription(subscriptionId: String): SubscriptionItem? {
        val json = subStorage.decodeString(subscriptionId) ?: return null
        return gson.fromJson(json, SubscriptionItem::class.java)
    }

    fun decodeAssetUrls(): List<Pair<String, AssetUrlItem>> {
        val assetUrlItems = mutableListOf<Pair<String, AssetUrlItem>>()
        assetStorage.allKeys()?.forEach { key ->
            val json = assetStorage.decodeString(key)
            if (!json.isNullOrBlank()) {
                assetUrlItems.add(Pair(key, gson.fromJson(json, AssetUrlItem::class.java)))
            }
        }
        return assetUrlItems.sortedBy { (_, value) -> value.addedTime }
    }

    fun removeAssetUrl(assetId: String) {
        assetStorage.remove(assetId)
    }

    fun encodeAsset(assetId: String, assetItem: AssetUrlItem) {
        val key = assetId.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        assetStorage.encode(key, gson.toJson(assetItem))
    }

    fun decodeAsset(assetid: String): AssetUrlItem? {
        val json = assetStorage.decodeString(assetid) ?: return null
        return gson.fromJson(json, AssetUrlItem::class.java)
    }

    fun encodeSettings(key: String, value: String?): Boolean {
        return settingsStorage.encode(key, value)
    }

    fun encodeSettings(key: String, value: Boolean): Boolean {
        return settingsStorage.encode(key, value)
    }

    fun encodeSettings(key: String, value: MutableSet<String>): Boolean {
        return settingsStorage.encode(key, value)
    }

    fun decodeRoutingRulesets(): MutableList<RulesetItem>? {
        val ruleset = settingsStorage.decodeString("pref_routing_ruleset")
        if (ruleset.isNullOrEmpty()) return null
        return gson.fromJson(ruleset, Array<RulesetItem>::class.java).toMutableList()
    }

    fun encodeRoutingRulesets(rulesetList: MutableList<RulesetItem>?) {
        if (rulesetList.isNullOrEmpty())
            encodeSettings("pref_routing_ruleset", "")
        else
            encodeSettings("pref_routing_ruleset", gson.toJson(rulesetList))
    }

    fun decodeSettingsString(key: String): String? {
        return settingsStorage.decodeString(key)
    }

    fun decodeSettingsString(key: String, defaultValue: String?): String? {
        return settingsStorage.decodeString(key, defaultValue)
    }

    fun decodeSettingsBool(key: String): Boolean {
        return settingsStorage.decodeBool(key, false)
    }

    fun decodeSettingsBool(key: String, defaultValue: Boolean): Boolean {
        return settingsStorage.decodeBool(key, defaultValue)
    }

    fun decodeSettingsStringSet(key: String): MutableSet<String>? {
        return settingsStorage.decodeStringSet(key)
    }

    fun decodeStartOnBoot(): Boolean {
        return decodeSettingsBool("pref_is_booted", false)
    }
}
package com.pet.vpn_client.data.mmkv

import com.google.gson.Gson
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.models.AssetUrlItem
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.RulesetItem
import com.pet.vpn_client.domain.models.ServerTestDelayInfo
import com.pet.vpn_client.domain.models.SubscriptionItem
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.tencent.mmkv.MMKV
import java.util.UUID
import javax.inject.Inject

class MMKVStorage @Inject constructor(private val gson: Gson) : KeyValueStorage {

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

    override fun getSelectServer(): String? {
        return mainStorage.decodeString(KEY_SELECTED_SERVER)
    }

    override fun setSelectServer(guid: String) {
        mainStorage.encode(KEY_SELECTED_SERVER, guid)
    }

    override fun encodeServerList(serverList: MutableList<String>) {
        mainStorage.encode(KEY_ANG_CONFIGS, gson.toJson(serverList))
    }

    override fun decodeServerList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_ANG_CONFIGS)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            //(json, Array<String>::class.java).toMutableList()
            gson.fromJson(json, mutableListOf<String>()::class.java)
        }
    }

    override fun decodeServerConfig(guid: String): ConfigProfileItem? {
        if (guid.isBlank()) {
            return null
        }
        val json = profileFullStorage.decodeString(guid)
        if (json.isNullOrBlank()) {
            return null
        }
        return gson.fromJson(json, ConfigProfileItem::class.java)
    }


    override fun encodeServerConfig(guid: String, config: ConfigProfileItem): String {
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

    override fun removeServer(guid: String) {
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

    override fun removeServerViaSubId(subId: String) {
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

    override fun decodeServerTestDelayInfo(guid: String): ServerTestDelayInfo? {
        if (guid.isBlank()) {
            return null
        }
        val json = serverTestDelayStorage.decodeString(guid)
        if (json.isNullOrBlank()) {
            return null
        }
        return gson.fromJson(json, ServerTestDelayInfo::class.java)
    }

    override fun encodeServerTestDelayInfo(guid: String, delayResult: Long) {
        if (guid.isBlank()) {
            return
        }
        val delay = decodeServerTestDelayInfo(guid) ?: ServerTestDelayInfo()
        delay.testDelayMillis = delayResult
        serverTestDelayStorage.encode(guid, gson.toJson(delay))
    }

    override fun clearAllTestDelayResults(keys: List<String>?) {
        keys?.forEach { key ->
            serverTestDelayStorage.encode(key, gson.toJson(ServerTestDelayInfo(0L)))
        }
    }

    override fun removeAllServer(): Int {
        val count = profileFullStorage.allKeys()?.count() ?: 0
        mainStorage.clearAll()
        profileFullStorage.clearAll()
        serverTestDelayStorage.clearAll()
        return count
    }

    override fun removeInvalidServer(guid: String): Int {
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

    override fun encodeServerRaw(guid: String, config: String) {
        serverRawStorage.encode(guid, config)
    }

    override fun decodeServerRaw(guid: String): String? {
        return serverRawStorage.decodeString(guid)
    }

    override fun encodeSubsList(subsList: MutableList<String>) {
        mainStorage.encode(KEY_SUB_IDS, gson.toJson(subsList))
    }

    override fun decodeSubsList(): MutableList<String> {
        val json = mainStorage.decodeString(KEY_SUB_IDS)
        return if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            gson.fromJson(json, mutableListOf<String>()::class.java)
        }
    }

    override fun decodeSubscriptions(): List<Pair<String, SubscriptionItem>> {

        val subsList = decodeSubsList()
        if (!subsList.isNotEmpty()) {
            subStorage.allKeys()?.forEach { key ->
                subsList.add(key)
            }
            encodeSubsList(subsList)
        }
        val subscriptions = mutableListOf<Pair<String, SubscriptionItem>>()
        decodeSubsList().forEach { key ->
            val json = subStorage.decodeString(key)
            if (!json.isNullOrBlank()) {
                subscriptions.add(Pair(key, gson.fromJson(json, SubscriptionItem::class.java)))
            }
        }
        return subscriptions
    }

    override fun removeSubscription(subId: String) {
        subStorage.remove(subId)
        val subsList = decodeSubsList()
        subsList.remove(subId)
        encodeSubsList(subsList)

        removeServerViaSubId(subId)
    }

    override fun encodeSubscription(guid: String, subItem: SubscriptionItem) {
        val key = guid.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        subStorage.encode(key, gson.toJson(subItem))

        val subsList = decodeSubsList()
        if (!subsList.contains(key)) {
            subsList.add(key)
            encodeSubsList(subsList)
        }
    }

    override fun decodeSubscription(subscriptionId: String): SubscriptionItem? {
        val json = subStorage.decodeString(subscriptionId) ?: return null
        return gson.fromJson(json, SubscriptionItem::class.java)
    }

    override fun decodeAssetUrls(): List<Pair<String, AssetUrlItem>> {
        val assetUrlItems = mutableListOf<Pair<String, AssetUrlItem>>()
        assetStorage.allKeys()?.forEach { key ->
            val json = assetStorage.decodeString(key)
            if (!json.isNullOrBlank()) {
                assetUrlItems.add(Pair(key, gson.fromJson(json, AssetUrlItem::class.java)))
            }
        }
        return assetUrlItems.sortedBy { (_, value) -> value.addedTime }
    }

    override fun removeAssetUrl(assetId: String) {
        assetStorage.remove(assetId)
    }

    override fun encodeAsset(assetId: String, assetItem: AssetUrlItem) {
        val key = assetId.ifBlank { UUID.randomUUID().toString().replace("-", "") }
        assetStorage.encode(key, gson.toJson(assetItem))
    }

    override fun decodeAsset(assetid: String): AssetUrlItem? {
        val json = assetStorage.decodeString(assetid) ?: return null
        return gson.fromJson(json, AssetUrlItem::class.java)
    }

    override fun encodeSettings(key: String, value: String?): Boolean {
        return settingsStorage.encode(key, value)
    }

    override fun encodeSettings(key: String, value: Boolean): Boolean {
        return settingsStorage.encode(key, value)
    }

    override fun encodeSettings(key: String, value: MutableSet<String>): Boolean {
        return settingsStorage.encode(key, value)
    }

    override fun decodeRoutingRulesets(): MutableList<RulesetItem>? {
        val ruleset = settingsStorage.decodeString(Constants.PREF_ROUTING_RULESET)
        if (ruleset.isNullOrEmpty()) return null
        return gson.fromJson(ruleset, Array<RulesetItem>::class.java).toMutableList()
    }

    override fun encodeRoutingRulesets(rulesetList: MutableList<RulesetItem>?) {
        if (rulesetList.isNullOrEmpty())
            encodeSettings(Constants.PREF_ROUTING_RULESET, "")
        else
            encodeSettings(Constants.PREF_ROUTING_RULESET, gson.toJson(rulesetList))
    }

    override fun decodeSettingsString(key: String): String? {
        return settingsStorage.decodeString(key)
    }

    override fun decodeSettingsString(key: String, defaultValue: String?): String? {
        return settingsStorage.decodeString(key, defaultValue)
    }

    override fun decodeSettingsBool(key: String): Boolean {
        return settingsStorage.decodeBool(key, false)
    }

    override fun decodeSettingsBool(key: String, defaultValue: Boolean): Boolean {
        return settingsStorage.decodeBool(key, defaultValue)
    }

    override fun decodeSettingsStringSet(key: String): MutableSet<String>? {
        return settingsStorage.decodeStringSet(key)
    }

    override fun decodeStartOnBoot(): Boolean {
        return decodeSettingsBool(Constants.PREF_IS_BOOTED, false)
    }

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
}
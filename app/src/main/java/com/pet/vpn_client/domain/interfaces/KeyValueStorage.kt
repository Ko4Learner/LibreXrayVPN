package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.AssetUrlItem
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.RulesetItem
import com.pet.vpn_client.domain.models.ServerTestDelayInfo
import com.pet.vpn_client.domain.models.SubscriptionItem

interface KeyValueStorage {

    fun setProxyMode()
    fun setVpnMode()
    fun getMode(): String

    fun getSelectServer(): String?

    fun setSelectServer(guid: String)

    fun encodeServerList(serverList: MutableList<String>)

    fun decodeServerList(): MutableList<String>

    fun decodeServerConfig(guid: String): ConfigProfileItem?

    fun encodeServerConfig(guid: String, config: ConfigProfileItem): String

    fun removeServer(guid: String)

    fun removeServerViaSubId()

    fun decodeServerTestDelayInfo(guid: String): ServerTestDelayInfo?

    fun encodeServerTestDelayInfo(guid: String, delayResult: Long)

    fun clearAllTestDelayResults(keys: List<String>?)

    fun removeAllServer(): Int

    fun removeInvalidServer(guid: String): Int

    fun encodeServerRaw(guid: String, config: String)

    fun decodeServerRaw(guid: String): String?

    fun encodeSubsList(subsList: MutableList<String>)

    fun decodeSubsList(): MutableList<String>

    fun decodeSubscriptions(): List<Pair<String, SubscriptionItem>>

    fun removeSubscription(subId: String)

    fun encodeSubscription(guid: String, subItem: SubscriptionItem)

    fun decodeSubscription(): SubscriptionItem?

    fun decodeAssetUrls(): List<Pair<String, AssetUrlItem>>

    fun removeAssetUrl(assetId: String)

    fun encodeAsset(assetId: String, assetItem: AssetUrlItem)

    fun decodeAsset(assetid: String): AssetUrlItem?

    fun encodeSettings(key: String, value: String?): Boolean

    fun encodeSettings(key: String, value: Boolean): Boolean

    fun encodeSettings(key: String, value: MutableSet<String>): Boolean

    fun decodeRoutingRulesets(): MutableList<RulesetItem>?

    fun encodeRoutingRulesets(rulesetList: MutableList<RulesetItem>?)

    fun decodeSettingsString(key: String): String?

    fun decodeSettingsString(key: String, defaultValue: String?): String?

    fun decodeSettingsBool(key: String): Boolean

    fun decodeSettingsBool(key: String, defaultValue: Boolean): Boolean

    fun decodeSettingsStringSet(key: String): MutableSet<String>?

    fun decodeStartOnBoot(): Boolean
}
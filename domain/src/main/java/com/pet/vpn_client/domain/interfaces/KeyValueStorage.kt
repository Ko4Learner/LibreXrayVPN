package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.ConfigProfileItem

interface KeyValueStorage {

    fun getSelectServer(): String?

    fun setSelectServer(guid: String)

    fun encodeServerList(serverList: MutableList<String>)

    fun decodeServerList(): MutableList<String>

    fun decodeServerConfig(guid: String): ConfigProfileItem?

    fun encodeServerConfig(guid: String, config: ConfigProfileItem): String

    fun removeServer(guid: String)

    fun encodeSettings(key: String, value: String?): Boolean

    fun decodeSettingsString(key: String): String?
}
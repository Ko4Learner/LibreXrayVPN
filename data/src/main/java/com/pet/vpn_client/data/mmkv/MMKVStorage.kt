package com.pet.vpn_client.data.mmkv

import com.google.gson.Gson
import com.pet.vpn_client.core.utils.Utils.fromJsonReified
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.tencent.mmkv.MMKV
import java.util.UUID
import javax.inject.Inject

class MMKVStorage @Inject constructor(private val gson: Gson) : KeyValueStorage {
    private val mainStorage by lazy { MMKV.mmkvWithID(ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val profileFullStorage by lazy { MMKV.mmkvWithID(ID_PROFILE_FULL_CONFIG, MMKV.MULTI_PROCESS_MODE) }
    private val settingsStorage by lazy { MMKV.mmkvWithID(ID_SETTING, MMKV.MULTI_PROCESS_MODE) }

    override fun getSelectedServer(): String? {
        return mainStorage.decodeString(KEY_SELECTED_SERVER)
    }

    override fun setSelectedServer(guid: String) {
        mainStorage.encode(KEY_SELECTED_SERVER, guid)
    }

    override fun encodeServerList(serverList: List<String>) {
        mainStorage.encode(KEY_ANG_CONFIGS, gson.toJson(serverList))
    }

    override fun decodeServerList(): List<String> {
        val json = mainStorage.decodeString(KEY_ANG_CONFIGS) ?: return emptyList()
        return runCatching { gson.fromJsonReified<List<String>>(json) }
            .getOrElse { emptyList() }
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
        val serverList = decodeServerList().toMutableList()
        if (!serverList.contains(key)) {
            serverList.add(0, key)
            encodeServerList(serverList)
            if (getSelectedServer().isNullOrBlank()) {
                mainStorage.encode(KEY_SELECTED_SERVER, key)
            }
        }
        return key
    }

    override fun removeServer(guid: String) {
        if (guid.isBlank()) {
            return
        }
        if (getSelectedServer() == guid) {
            mainStorage.remove(KEY_SELECTED_SERVER)
        }
        val serverList = decodeServerList().toMutableList()
        serverList.remove(guid)
        encodeServerList(serverList)
        profileFullStorage.remove(guid)
    }

    override fun encodeSettings(key: String, value: String?): Boolean {
        return settingsStorage.encode(key, value)
    }

    override fun decodeSettingsString(key: String): String? {
        return settingsStorage.decodeString(key)
    }

    companion object {
        private const val ID_MAIN = "MAIN"
        private const val ID_PROFILE_FULL_CONFIG = "PROFILE_FULL_CONFIG"
        private const val ID_SETTING = "SETTING"
        private const val KEY_SELECTED_SERVER = "SELECTED_SERVER"
        private const val KEY_ANG_CONFIGS = "ANG_CONFIGS"
    }
}
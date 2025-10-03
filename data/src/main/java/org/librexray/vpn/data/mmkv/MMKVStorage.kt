package org.librexray.vpn.data.mmkv

import com.google.gson.Gson
import org.librexray.vpn.coreandroid.utils.Utils.fromJsonReified
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import com.tencent.mmkv.MMKV
import java.util.UUID
import javax.inject.Inject

/**
 * A Key-Value storage implementation backed by MMKV.
 */
class MMKVStorage @Inject constructor(private val gson: Gson) : KeyValueStorage {
    private val mainStorage by lazy { MMKV.mmkvWithID(ID_MAIN, MMKV.SINGLE_PROCESS_MODE) }
    private val profileFullStorage by lazy { MMKV.mmkvWithID(ID_PROFILE_FULL_CONFIG, MMKV.SINGLE_PROCESS_MODE) }
    private val settingsStorage by lazy { MMKV.mmkvWithID(ID_SETTING, MMKV.SINGLE_PROCESS_MODE) }

    /**
     * Returns the GUID of the currently selected server, if any.
     */
    override fun getSelectedServer(): String? {
        return mainStorage.decodeString(KEY_SELECTED_SERVER)
    }

    /**
     * Persists the GUID of the selected server.
     */
    override fun setSelectedServer(guid: String) {
        mainStorage.encode(KEY_SELECTED_SERVER, guid)
    }

    /**
     * Stores the ordered list of server GUIDs.
     */
    override fun encodeServerList(serverList: List<String>) {
        mainStorage.encode(KEY_SERVER_CONFIGS, gson.toJson(serverList))
    }

    /**
     * Reads the ordered list of server GUIDs.
     */
    override fun decodeServerList(): List<String> {
        val json = mainStorage.decodeString(KEY_SERVER_CONFIGS) ?: return emptyList()
        return runCatching { gson.fromJsonReified<List<String>>(json) }
            .getOrElse { emptyList() }
    }

    /**
     * Writes a full profile to the store and ensures the GUID is present at the head of the server list.
     * If there is no selected server yet, the newly written GUID becomes selected.
     */
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

    /**
     * Reads a full profile by GUID and deserializes it from JSON.
     */
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

    /**
     * Removes a server profile and its GUID from all storages.
     *
     * If the removed server was selected, the selection flag is cleared.
     */
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

    /**
     * Writes a String setting into the settings store.
     *
     * @param key Setting key.
     * @param value Value to save.
     */
    override fun encodeSettingsString(key: String, value: String?) {
        settingsStorage.encode(key, value)
    }

    /**
     * Reads a String setting from the settings store.
     *
     * @param key Setting key.
     * @return Stored value or `null` when absent.
     */
    override fun decodeSettingsString(key: String): String? {
        return settingsStorage.decodeString(key)
    }

    /**
     * Writes a Boolean setting into the settings store.
     *
     * @param key Setting key.
     * @param value Value to save.
     */
    override fun encodeSettingsBoolean(key: String, value: Boolean) {
        settingsStorage.encode(key, value)
    }

    /**
     * Reads a Boolean setting from the settings store.
     *
     * @param key Setting key.
     * @return Stored value.
     */
    override fun decodeSettingsBoolean(key: String): Boolean {
        return settingsStorage.decodeBool(key)
    }

    companion object {
        private const val ID_MAIN = "MAIN"
        private const val ID_PROFILE_FULL_CONFIG = "PROFILE_FULL_CONFIG"
        private const val ID_SETTING = "SETTING"
        private const val KEY_SELECTED_SERVER = "SELECTED_SERVER"
        private const val KEY_SERVER_CONFIGS = "SERVER_CONFIGS"
    }
}
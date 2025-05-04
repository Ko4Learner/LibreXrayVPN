package com.pet.vpn_client.data.mmkv

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import javax.inject.Inject

class MMKVConfig @Inject constructor(private val gson: Gson) {

    companion object {
        private const val ID_MAIN = "MAIN"
        private const val ID_PROFILE_FULL_CONFIG = "PROFILE_FULL_CONFIG"
        private const val ID_SERVER_RAW = "SERVER_RAW"
        private const val ID_SERVER_AFF = "SERVER_AFF"
        private const val ID_SUB = "SUB"
        private const val ID_ASSET = "ASSET"
        private const val ID_SETTING = "SETTING"
        private const val KEY_SELECTED_SERVER = "SELECTED_SERVER"
        private const val KEY_ANG_CONFIGS = "ANG_CONFIGS"
        private const val KEY_SUB_IDS = "SUB_IDS"
    }

    private val mainStorage by lazy { MMKV.mmkvWithID(ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val profileFullStorage by lazy { MMKV.mmkvWithID(ID_PROFILE_FULL_CONFIG, MMKV.MULTI_PROCESS_MODE) }
    private val serverRawStorage by lazy { MMKV.mmkvWithID(ID_SERVER_RAW, MMKV.MULTI_PROCESS_MODE) }
    private val serverAffStorage by lazy { MMKV.mmkvWithID(ID_SERVER_AFF, MMKV.MULTI_PROCESS_MODE) }
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
}
package com.pet.vpn_client.data

import android.content.Context

class ConfigManager {
    private var initConfigCache: String? = null

    //region get config function

//    fun getV2rayConfig(context: Context, guid: String): ConfigResult {
//        try {
//            val config = MmkvManager.decodeServerConfig(guid) ?: return ConfigResult(false)
//            return if (config.configType == EConfigType.CUSTOM) {
//                getV2rayCustomConfig(guid, config)
//            } else {
//                getV2rayNormalConfig(context, guid, config)
//            }
//        } catch (e: Exception) {
//            Log.e(AppConfig.TAG, "Failed to get V2ray config", e)
//            return ConfigResult(false)
//        }
//    }
}

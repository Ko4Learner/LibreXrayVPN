package com.pet.vpn_client.framework.bridge

import android.content.Context
import android.provider.Settings
import android.util.Base64
import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import go.Seq
import libv2ray.Libv2ray

object XrayInitializer {
    private const val DIR_ASSETS = "assets"

    fun init(context: Context) {
        Seq.setContext(context)
        Libv2ray.initCoreEnv(
            userAssetPath(context),
            getDeviceIdForXUDPBaseKey()
        )
    }

    private fun userAssetPath(context: Context?): String {
        if (context == null) return ""
        return try {
            context.getExternalFilesDir(DIR_ASSETS)?.absolutePath
                ?: context.getDir(DIR_ASSETS, 0).absolutePath
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get user asset path", e)
            ""
        }
    }

    private fun getDeviceIdForXUDPBaseKey(): String {
        return try {
            val androidId = Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8)
            Base64.encodeToString(androidId.copyOf(32), Base64.NO_PADDING.or(Base64.URL_SAFE))
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to generate device ID", e)
            ""
        }
    }
}
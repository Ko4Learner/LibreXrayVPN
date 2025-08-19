package com.pet.vpn_client.framework.bridge_to_core

import android.content.Context
import android.provider.Settings
import android.util.Base64
import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import go.Seq
import libv2ray.Libv2ray

/**
 * Provides initialization logic for the Xray core environment.
 * Sets up necessary directories and unique device parameters required by Xray.
 */
object XrayInitializer {
    private const val DIR_ASSETS = "assets"

    /**
     * Initializes the Xray core environment.
     *
     * @param context Application context, required for accessing app directories.
     *
     * - Sets context for dependent libraries.
     * - Initializes Xray with asset path and unique device ID.
     */
    fun init(context: Context) {
        Seq.setContext(context)
        Libv2ray.initCoreEnv(
            userAssetPath(context),
            getDeviceIdForXUDPBaseKey()
        )
    }

    /**
     * Returns the path to the directory with user assets.
     *
     * @param context Context used to resolve file directories.
     * @return Absolute path to assets directory or empty string if not found.
     *
     * - Tries to use external files directory first.
     * - Falls back to internal directory if external is unavailable.
     * - Returns empty string if an error occurs.
     */
    private fun userAssetPath(context: Context): String {
        return try {
            context.getExternalFilesDir(DIR_ASSETS)?.absolutePath
                ?: context.getDir(DIR_ASSETS, 0).absolutePath
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get user asset path", e)
            ""
        }
    }

    /**
     * Generates a unique device ID for use as a base key in Xray.
     *
     * @return Base64-encoded Android ID, or empty string if an error occurs.
     *
     * - Gets the device's ANDROID_ID.
     * - Encodes it as Base64 string with no padding and URL-safe characters.
     * - Returns empty string on failure.
     */
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
package com.pet.vpn_client.app

import android.app.Application
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.data.mmkv.MMKVInitializer
import com.pet.vpn_client.framework.bridge_to_core.XrayInitializer
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for the VPN client.
 *
 * Responsibilities:
 * - Enables Hilt dependency injection (@HiltAndroidApp).
 * - Initializes app-wide storage (MMKV) and VPN core engine (Xray).
 * - Configures a global error handler (Utils.error) to log errors using Android's Log.e.
 */
@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKVInitializer.init(this)
        XrayInitializer.init(context = this)
        Utils.error = { msg, tr -> android.util.Log.e(Constants.TAG, msg, tr) }
    }
}
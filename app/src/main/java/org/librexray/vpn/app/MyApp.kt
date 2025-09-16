package org.librexray.vpn.app

import android.app.Application
import android.util.Log
import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.coreandroid.utils.Utils
import org.librexray.vpn.data.mmkv.MMKVInitializer
import org.librexray.vpn.framework.bridge_to_core.XrayInitializer
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
        Utils.error = { msg, tr -> Log.e(Constants.TAG, msg, tr) }
    }
}
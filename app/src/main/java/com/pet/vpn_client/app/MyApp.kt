package com.pet.vpn_client.app

import android.app.Application
import com.pet.vpn_client.data.mmkv.MMKVInitializer
import com.pet.vpn_client.framework.bridge_to_core.XrayInitializer
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for the VPN client.
 *
 * Responsibilities:
 * - Enables Hilt dependency injection (@HiltAndroidApp).
 * - Initializes app-wide storage (MMKV) and VPN core engine (Xray).
 */
@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKVInitializer.init(this)
        XrayInitializer.init(context = this)
    }
}
package com.pet.vpn_client.app

import android.app.Application
import com.pet.vpn_client.data.mmkv.MMKVInitializer
import com.pet.vpn_client.framework.bridge.XrayInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKVInitializer.init(this)
        XrayInitializer.init(context = this)
    }
}
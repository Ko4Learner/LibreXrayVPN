package com.pet.vpn_client.app

import android.app.Application
import com.pet.vpn_client.utils.Utils
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import go.Seq
import libv2ray.Libv2ray

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        Seq.setContext(applicationContext)
        Libv2ray.initCoreEnv(
            Utils.userAssetPath(applicationContext),
            Utils.getDeviceIdForXUDPBaseKey()
        )
    }
}
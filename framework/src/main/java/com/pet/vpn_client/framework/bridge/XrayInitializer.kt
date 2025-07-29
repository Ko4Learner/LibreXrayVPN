package com.pet.vpn_client.framework.bridge

import android.content.Context
import com.pet.vpn_client.core.utils.Utils
import go.Seq
import libv2ray.Libv2ray

object XrayInitializer {
    fun init(context: Context) {
        Seq.setContext(context)
        Libv2ray.initCoreEnv(
            Utils.userAssetPath(context),
            Utils.getDeviceIdForXUDPBaseKey()
        )
    }
}
package com.pet.vpn_client.data.mmkv

import android.content.Context
import com.tencent.mmkv.MMKV

object MMKVInitializer {
    fun init(context: Context) {
        MMKV.initialize(context)
    }
}
package org.librexray.vpn.data.mmkv

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * Initializes the MMKV storage library for the application.
 */
object MMKVInitializer {
    fun init(context: Context) {
        MMKV.initialize(context)
    }
}
package com.pet.vpn_client.domain.interfaces

import android.content.BroadcastReceiver
import android.content.Context

interface ServiceManager {
    fun setService(service: ServiceControl)
    fun getService(): ServiceControl?
    fun getMsgReceive(): BroadcastReceiver
    fun startServiceFromToggle(context: Context): Boolean
    fun startService(context: Context, guid: String? = null)
    fun stopService()
    fun getRunningServerName(): String
    fun startCoreLoop(): Boolean
    fun stopCoreLoop(): Boolean
    fun measureDelay(time: Long)
    fun registerReceiver()
    fun unregisterReceiver()

}
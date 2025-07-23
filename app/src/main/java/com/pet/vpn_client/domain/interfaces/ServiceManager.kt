package com.pet.vpn_client.domain.interfaces

interface ServiceManager {
    fun setService(service: ServiceControl)
    fun getService(): ServiceControl?

    //    fun getMsgReceive(): BroadcastReceiver
    fun startServiceFromToggle(): Boolean
    fun startService(guid: String? = null)
    fun stopService()
    fun getRunningServerName(): String
    fun startCoreLoop(): Boolean
    fun stopCoreLoop(): Boolean
    suspend fun measureDelay(): Long?
    fun registerReceiver(): Boolean
    fun unregisterReceiver()
    fun restartService()
}
package com.pet.vpn_client.domain.interfaces

interface ServiceManager {
    fun startServiceFromToggle(): Boolean
    fun startService(guid: String? = null)
    fun stopService()
    fun getRunningServerName(): String
    fun startCoreLoop(): Boolean
    fun stopCoreLoop()
    suspend fun measureDelay(): Long?
    fun restartService()
}
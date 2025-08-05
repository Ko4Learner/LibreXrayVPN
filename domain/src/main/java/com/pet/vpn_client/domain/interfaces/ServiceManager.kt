package com.pet.vpn_client.domain.interfaces

interface ServiceManager {
    fun startService(): Boolean
    fun stopService()
    fun getRunningServerName(): String
    fun startCoreLoop(): Boolean
    fun stopCoreLoop()
    suspend fun measureDelay(): Long?
    fun restartService()
}
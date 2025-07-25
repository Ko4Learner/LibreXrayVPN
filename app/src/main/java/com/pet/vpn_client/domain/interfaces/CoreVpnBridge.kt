package com.pet.vpn_client.domain.interfaces

interface CoreVpnBridge {
    fun isRunning(): Boolean
    fun startCoreLoop(): Boolean
    fun stopCoreLoop()
    fun queryStats(tag: String, link: String): Long
    suspend fun measureDelay(): Long?
}
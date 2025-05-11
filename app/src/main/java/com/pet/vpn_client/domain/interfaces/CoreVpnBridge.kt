package com.pet.vpn_client.domain.interfaces

interface CoreVpnBridge {
    fun isRunning(): Boolean
    fun startCoreLoop(): Boolean
    fun stopCoreLoop(): Boolean
    fun queryStats(tag: String, link: String): Long
    fun measureV2rayDelay()
}
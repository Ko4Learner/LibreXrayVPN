package com.pet.vpn_client.domain.interfaces

/**
 * Bridge to the VPN core engine (e.g., Xray) at the domain level.
 *
 * Contracts:
 * - isRunning():
 *     Thread-safe check whether the core loop/process is currently active.
 *
 * - startCoreLoop():
 *
 * - stopCoreLoop():
 *
 * - queryStats(tag, link):
 *
 * - measureDelay():
 *     Measures round-trip latency to an engine-defined target and returns it in **milliseconds**,
 */
interface CoreVpnBridge {
    fun isRunning(): Boolean
    fun startCoreLoop(): Boolean
    fun stopCoreLoop()
    fun queryStats(tag: String, link: String): Long
    suspend fun measureDelay(): Long?
}
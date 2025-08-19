package com.pet.vpn_client.domain.interfaces

import kotlinx.coroutines.flow.StateFlow

/**
 * Domain-level bridge to the underlying VPN core engine (e.g., Xray).
 *
 * Contracts:
 * - `coreState`: **hot** [StateFlow] that reflects whether the core loop/process is currently active.
 * - `startCoreLoop()`: Attempts to start the core loop; returns `true` if the start was initiated
 * - `stopCoreLoop()`: stops the core loop if running.
 * - `queryStats(tag, link)`: Retrieves a stat counter from the core (e.g., traffic in bytes) by tag and link.
 * - `measureDelay()`: Measures round-trip latency to an engine-defined target and returns it in **milliseconds**,
 *   or `null` if measurement is not possible.
 */
interface CoreVpnBridge {
    val coreState: StateFlow<Boolean>
    fun startCoreLoop(): Boolean
    fun stopCoreLoop()
    fun queryStats(tag: String, link: String): Long
    suspend fun measureDelay(): Long?
}
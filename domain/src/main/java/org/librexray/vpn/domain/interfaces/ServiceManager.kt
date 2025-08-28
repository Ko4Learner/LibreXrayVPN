package org.librexray.vpn.domain.interfaces

/**
 * Orchestrates the VPN service lifecycle and delegates core engine control when needed.
 * Contracts:
 * - startService():
 *     Initiates the VPN service start using the currently selected profile.
 *
 * - stopService():
 *     Requests a graceful shutdown.
 *
 * - getRunningServerName():
 *     Returns the display name for the currently running profile.
 *
 * - startCoreLoop():
 *     Initiates starting the core engine loop. Returns `true` if the request was accepted/initiated;
 *
 * - stopCoreLoop():
 *     Requests the core engine loop to stop.
 *
 * - restartService():
 *
 * - measureDelay():
 *     Measures round-trip latency to a predefined target and returns it in **milliseconds**,
 */
interface ServiceManager {
    fun startService()
    fun stopService()
    fun getRunningServerName(): String
    fun startCoreLoop(): Boolean
    fun stopCoreLoop()
    fun restartService()
    suspend fun measureDelay(): Long?
}
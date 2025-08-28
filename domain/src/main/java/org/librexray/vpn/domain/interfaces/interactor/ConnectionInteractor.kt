package org.librexray.vpn.domain.interfaces.interactor

import org.librexray.vpn.domain.models.ConnectionSpeed
import kotlinx.coroutines.flow.Flow

/**
 * Controls VPN lifecycle at the domain level.
 *
 * Contracts:
 * - startConnection()
 * - stopConnection()
 * - testConnection(): returns round-trip latency in **ms**, or null if failed.
 * - restartConnection()
 * - observeTagSpeed(): Exposes a continuous [Flow] of [ConnectionSpeed] samples
 *   for both proxy and direct channels.
 */
interface ConnectionInteractor {
    suspend fun startConnection()
    suspend fun stopConnection()
    suspend fun testConnection(): Long?
    fun restartConnection()
    fun observeSpeed(): Flow<ConnectionSpeed>
}
package com.pet.vpn_client.domain.interfaces.interactor

import com.pet.vpn_client.domain.models.TagSpeed
import kotlinx.coroutines.flow.Flow

/**
 * Controls VPN lifecycle at the domain level.
 *
 * Contracts:
 * - startConnection()
 * - stopConnection()
 * - testConnection(): returns round-trip latency in **ms**, or null if failed.
 * - restartConnection()
 */
interface ConnectionInteractor {
    suspend fun startConnection()
    suspend fun stopConnection()
    suspend fun testConnection(): Long?
    fun restartConnection()
    fun observeTagSpeed(
        tags: List<String>,
        periodMs: Long = 3000
    ): Flow<List<TagSpeed>>
}
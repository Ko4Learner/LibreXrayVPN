package com.pet.vpn_client.domain.interactor_impl

import android.os.SystemClock
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.domain.models.TagSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject

class ConnectionInteractorImpl @Inject constructor(
    private val serviceManager: ServiceManager,
    private val coreVpnBridge: CoreVpnBridge
) :
    ConnectionInteractor {
    override suspend fun startConnection() {
        serviceManager.startService()
    }

    override suspend fun stopConnection() {
        serviceManager.stopService()
    }

    override suspend fun testConnection(): Long? {
        return serviceManager.measureDelay()
    }

    override fun restartConnection() {
        serviceManager.restartService()
    }

    override fun observeTagSpeed(
        tags: List<String>,
        periodMs: Long
    ): Flow<List<TagSpeed>> = flow {
        val allTags = (tags + "direct").distinct()

        var prev: Map<String, Pair<Long, Long>>? = null
        var prevT = SystemClock.elapsedRealtime()

        while (currentCoroutineContext().isActive) {
            val now = SystemClock.elapsedRealtime()
            val dtSec = ((now - prevT).coerceAtLeast(1)).toDouble() / 1000.0

            val curr = allTags.associateWith { tag ->
                val up = coreVpnBridge.queryStats(tag, "uplink")
                val down = coreVpnBridge.queryStats(tag, "downlink")
                up to down
            }

            val speeds = if (prev == null) {
                curr.map { (tag, _) -> TagSpeed(tag, 0.0, 0.0, now) }
            } else {
                curr.map { (tag, nowPair) ->
                    val (pu, pd) = prev[tag] ?: (0L to 0L)
                    TagSpeed(
                        tag = tag,
                        uplinkBps = (nowPair.first - pu).coerceAtLeast(0) / dtSec,
                        downlinkBps = (nowPair.second - pd).coerceAtLeast(0) / dtSec,
                        timestampMs = now
                    )
                }
            }

            emit(speeds.sortedBy { it.tag })
            prev = curr
            prevT = now
            delay(periodMs)
        }
    }.flowOn(Dispatchers.IO)
}
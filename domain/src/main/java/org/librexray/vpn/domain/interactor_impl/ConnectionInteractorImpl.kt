package org.librexray.vpn.domain.interactor_impl

import android.os.SystemClock
import org.librexray.vpn.domain.interfaces.CoreVpnBridge
import org.librexray.vpn.domain.interfaces.ServiceManager
import org.librexray.vpn.domain.interfaces.interactor.ConnectionInteractor
import org.librexray.vpn.domain.models.ConnectionSpeed
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

    /**
     * Observes connection throughput statistics by periodically querying the VPN core.
     *
     * Implementation details:
     * - The method launches a cold [Flow] that, once collected, enters an infinite loop
     *   (until the coroutine scope is cancelled).
     * - Each iteration:
     *   1. Captures the current time and queries VPN core stats for proxy and direct
     *      uplink/downlink counters (monotonic byte totals).
     *   2. Computes elapsed time since the previous iteration (`deltaTimeSeconds`).
     *   3. Derives per-second rates by subtracting the previous counters and dividing
     *      by the elapsed time.
     *   4. Emits a [ConnectionSpeed] sample.
     * - The very first emission is always zeros because no baseline is available yet.
     *
     * Timing:
     * - The loop sleeps for [PERIOD_MS] (default 3 seconds) between samples.
     * - [SystemClock.elapsedRealtime] is used to ensure monotonic timing resilient to wall clock changes.
     *
     * Threading:
     * - The flow runs on [Dispatchers.IO] via [flowOn], making it safe to collect from main.
     *
     * Cancellation:
     * - Terminates cleanly when the collector scope is cancelled.
     */
    override fun observeSpeed(): Flow<ConnectionSpeed> = flow {
        var prevProxy: Pair<Long, Long>? = null
        var prevDirect: Pair<Long, Long>? = null
        var prevTime = SystemClock.elapsedRealtime()

        while (currentCoroutineContext().isActive) {
            val now = SystemClock.elapsedRealtime()
            val deltaTimeSeconds = ((now - prevTime).coerceAtLeast(1)).toDouble() / 1000.0

            val proxyNow = coreVpnBridge.queryStats(TAG_PROXY, TAG_UPLINK) to
                    coreVpnBridge.queryStats(TAG_PROXY, TAG_DOWNLINK)
            val directNow = coreVpnBridge.queryStats(TAG_DIRECT, TAG_UPLINK) to
                    coreVpnBridge.queryStats(TAG_DIRECT, TAG_DOWNLINK)

            val speeds = if (prevProxy == null || prevDirect == null) {
                ConnectionSpeed(0.0, 0.0, 0.0, 0.0)
            } else {
                ConnectionSpeed(
                    proxyUplinkBps = (proxyNow.first - prevProxy.first).coerceAtLeast(0) / deltaTimeSeconds,
                    proxyDownlinkBps = (proxyNow.second - prevProxy.second).coerceAtLeast(0) / deltaTimeSeconds,
                    directUplinkBps = (directNow.first - prevDirect.first).coerceAtLeast(0) / deltaTimeSeconds,
                    directDownlinkBps = (directNow.second - prevDirect.second).coerceAtLeast(0) / deltaTimeSeconds
                )
            }

            emit(speeds)

            prevProxy = proxyNow
            prevDirect = directNow
            prevTime = now
            delay(PERIOD_MS)
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val PERIOD_MS = 3000L
        private const val TAG_PROXY = "proxy"
        private const val TAG_DIRECT = "direct"
        private const val TAG_UPLINK = "uplink"
        private const val TAG_DOWNLINK = "downlink"
    }
}
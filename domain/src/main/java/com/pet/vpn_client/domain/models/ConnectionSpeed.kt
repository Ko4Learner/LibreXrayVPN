package com.pet.vpn_client.domain.models

/**
 * Represents measured traffic throughput across VPN channels.
 *
 * Units: all speeds are expressed in **bytes per second (Bps)**, averaged over the sampling interval.
 *
 * @property proxyUplinkBps    Upload throughput via VPN tunnel (proxy).
 * @property proxyDownlinkBps  Download throughput via VPN tunnel (proxy).
 * @property directUplinkBps   Upload throughput via direct/non-proxied link.
 * @property directDownlinkBps Download throughput via direct/non-proxied link.
 */
data class ConnectionSpeed(
    val proxyUplinkBps: Double,
    val proxyDownlinkBps: Double,
    val directUplinkBps: Double,
    val directDownlinkBps: Double
)
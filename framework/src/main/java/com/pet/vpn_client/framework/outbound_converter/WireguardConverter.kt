package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.bridge.XrayConfigProvider
import dagger.Lazy
import javax.inject.Inject

/**
 * Converts a profile into an Xray WireGuard outbound.
 *
 * Responsibilities:
 * - Maps keys, local addresses, peer parameters (publicKey/preSharedKey/endpoint),
 *   and optional MTU/reserved values.
 * - Leaves transport/TLS settings unused (not applicable to WireGuard).
 */
class WireguardConverter @Inject constructor(private val xrayConfigProviderLazy: Lazy<XrayConfigProvider>) {

    /**
     * Builds a WireGuard [OutboundBean] from the given profile.
     */
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val xrayConfigProvider = xrayConfigProviderLazy.get()
        val outboundBean = xrayConfigProvider.createInitOutbound(EConfigType.WIREGUARD)

        outboundBean?.settings?.let { wireguard ->
            wireguard.secretKey = profileItem.secretKey
            wireguard.address =
                (profileItem.localAddress ?: Constants.WIREGUARD_LOCAL_ADDRESS_V4).split(",")
            wireguard.peers?.firstOrNull()?.let { peer ->
                peer.publicKey = profileItem.publicKey.orEmpty()
                peer.preSharedKey = profileItem.preSharedKey?.takeIf { it.isNotEmpty() }
                peer.endpoint =
                    Utils.getIpv6Address(profileItem.server) + ":${profileItem.serverPort}"
            }
            wireguard.mtu = profileItem.mtu
            wireguard.reserved = profileItem.reserved?.takeIf { it.isNotBlank() }?.split(",")
                ?.filter { it.isNotBlank() }?.map { it.trim().toInt() }
        }
        return outboundBean
    }
}
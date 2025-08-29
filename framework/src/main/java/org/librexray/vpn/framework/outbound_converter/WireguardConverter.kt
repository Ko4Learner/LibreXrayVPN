/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.framework.outbound_converter

import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.framework.models.XrayConfig.OutboundBean
import org.librexray.vpn.framework.bridge_to_core.XrayConfigProvider
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
        val outboundBean = xrayConfigProvider.createInitOutbound(ConfigType.WIREGUARD)

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
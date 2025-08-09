package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.bridge.XrayConfigProvider
import dagger.Lazy
import javax.inject.Inject

/**
 * Converts a profile into an Xray Trojan outbound.
 *
 * Responsibilities:
 * - Maps server address/port, password and optional flow.
 * - Applies transport settings and then TLS/REALITY options when applicable.
 */
class TrojanConverter @Inject constructor(private val xrayConfigProviderLazy: Lazy<XrayConfigProvider>) {

    /**
     * Builds a Trojan [OutboundBean] from the given profile.
     */
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val xrayConfigProvider = xrayConfigProviderLazy.get()
        val outboundBean = xrayConfigProvider.createInitOutbound(EConfigType.TROJAN)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            server.password = profileItem.password
            server.flow = profileItem.flow
        }

        val sni = outboundBean?.streamSettings?.let {
            xrayConfigProvider.populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            xrayConfigProvider.populateTlsSettings(it, profileItem, sni)
        }
        return outboundBean
    }
}
package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.bridge.XrayConfigProvider
import dagger.Lazy
import javax.inject.Inject

/**
 * Converts a profile into an Xray Shadowsocks outbound.
 *
 * Responsibilities:
 * - Maps server address/port, method and password.
 * - Applies transport settings and then TLS/REALITY options when applicable.
 */
class ShadowsocksConverter @Inject constructor(private val xrayConfigProviderLazy: Lazy<XrayConfigProvider>) {

    /**
     * Builds a Shadowsocks [OutboundBean] from the given profile.
     */
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val xrayConfigProvider = xrayConfigProviderLazy.get()
        val outboundBean = xrayConfigProvider.createInitOutbound(EConfigType.SHADOWSOCKS)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            server.password = profileItem.password
            server.method = profileItem.method
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
package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.bridge.XrayConfigProvider
import dagger.Lazy
import javax.inject.Inject

/**
 * Converts a profile into an Xray VLESS outbound.
 *
 * Responsibilities:
 * - Maps server address/port and user fields (id/encryption/flow).
 * - Applies transport settings and then TLS/REALITY options when applicable.
 */
class VlessConverter @Inject constructor(private val xrayConfigProviderLazy: Lazy<XrayConfigProvider>) {

    /**
     * Builds a VLESS [OutboundBean] from the given profile.
     */
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val xrayConfigProvider = xrayConfigProviderLazy.get()
        val outboundBean = xrayConfigProvider.createInitOutbound(EConfigType.VLESS)

        outboundBean?.settings?.vnext?.first()?.let { vnext ->
            vnext.address = profileItem.server.orEmpty()
            vnext.port = profileItem.serverPort.orEmpty().toInt()
            vnext.users[0].id = profileItem.password.orEmpty()
            vnext.users[0].encryption = profileItem.method
            vnext.users[0].flow = profileItem.flow
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
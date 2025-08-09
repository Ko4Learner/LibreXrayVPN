package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.bridge.XrayConfigProvider
import dagger.Lazy
import javax.inject.Inject


class ShadowsocksConverter @Inject constructor(private val xrayConfigProviderLazy: Lazy<XrayConfigProvider>) {
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
package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.interfaces.CoreConfigProvider
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import javax.inject.Inject
import javax.inject.Provider

class TrojanConverter @Inject constructor(val coreConfigProvider: Provider<CoreConfigProvider>) {
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = coreConfigProvider.get().createInitOutbound(EConfigType.TROJAN)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            server.password = profileItem.password
            server.flow = profileItem.flow
        }

        val sni = outboundBean?.streamSettings?.let {
            coreConfigProvider.get().populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            coreConfigProvider.get().populateTlsSettings(it, profileItem, sni)
        }
        return outboundBean
    }
}
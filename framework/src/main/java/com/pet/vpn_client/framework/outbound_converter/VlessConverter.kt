package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.interfaces.CoreConfigProvider
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import javax.inject.Inject
import javax.inject.Provider

class VlessConverter @Inject constructor(val coreConfigProvider: Provider<CoreConfigProvider>) {
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = coreConfigProvider.get().createInitOutbound(EConfigType.VLESS)

        outboundBean?.settings?.vnext?.first()?.let { vnext ->
            vnext.address = profileItem.server.orEmpty()
            vnext.port = profileItem.serverPort.orEmpty().toInt()
            vnext.users[0].id = profileItem.password.orEmpty()
            vnext.users[0].encryption = profileItem.method
            vnext.users[0].flow = profileItem.flow
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
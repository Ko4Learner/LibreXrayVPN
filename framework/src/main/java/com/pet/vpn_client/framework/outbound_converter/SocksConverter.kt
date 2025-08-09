package com.pet.vpn_client.framework.outbound_converter

import com.pet.vpn_client.domain.interfaces.CoreConfigProvider
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import javax.inject.Inject
import javax.inject.Provider

class SocksConverter @Inject constructor(val coreConfigProvider: Provider<CoreConfigProvider>) {
    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = coreConfigProvider.get().createInitOutbound(EConfigType.SOCKS)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            if (!profileItem.username.isNullOrEmpty()) {
                val socksUsersBean = OutboundBean.OutSettingsBean.ServersBean.SocksUsersBean()
                socksUsersBean.user = profileItem.username.orEmpty()
                socksUsersBean.pass = profileItem.password.orEmpty()
                server.users = listOf(socksUsersBean)
            }
        }

        return outboundBean
    }
}
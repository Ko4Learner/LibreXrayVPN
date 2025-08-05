package com.pet.vpn_client.data.config_formatter

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider


class WireguardFormatter @Inject constructor(
    val configManager: Provider<ConfigManager>,
    val storage: KeyValueStorage
) : BaseFormatter() {

    fun parse(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(EConfigType.WIREGUARD)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        config.secretKey = uri.userInfo.orEmpty()
        config.localAddress = queryParam["address"] ?: Constants.WIREGUARD_LOCAL_ADDRESS_V4
        config.publicKey = queryParam["publickey"].orEmpty()
        config.preSharedKey = queryParam["presharedkey"]?.takeIf { it.isNotEmpty() }
        config.mtu = Utils.parseInt(queryParam["mtu"] ?: Constants.WIREGUARD_LOCAL_MTU)
        config.reserved = queryParam["reserved"] ?: "0,0,0"

        return config
    }

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configManager.get().createInitOutbound(EConfigType.WIREGUARD)

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
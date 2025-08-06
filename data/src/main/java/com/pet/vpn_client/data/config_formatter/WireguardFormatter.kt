package com.pet.vpn_client.data.config_formatter

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
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
    val configRepository: Provider<ConfigRepository>,
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
        config.localAddress = queryParam[QUERY_ADDRESS] ?: Constants.WIREGUARD_LOCAL_ADDRESS_V4
        config.publicKey = queryParam[QUERY_PUBLIC_KEY].orEmpty()
        config.preSharedKey = queryParam[QUERY_PRESHARED_KEY]?.takeIf { it.isNotEmpty() }
        config.mtu = (queryParam[QUERY_MTU] ?: Constants.WIREGUARD_LOCAL_MTU).toIntOrNull() ?: 0
        config.reserved = queryParam[QUERY_RESERVED] ?: DEFAULT_RESERVED

        return config
    }

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configRepository.get().createInitOutbound(EConfigType.WIREGUARD)

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

    companion object {
        private const val QUERY_ADDRESS = "address"
        private const val QUERY_PUBLIC_KEY = "publickey"
        private const val QUERY_PRESHARED_KEY = "presharedkey"
        private const val QUERY_MTU = "mtu"
        private const val QUERY_RESERVED = "reserved"

        private const val DEFAULT_RESERVED = "0,0,0"
    }
}
package com.pet.vpn_client.data.config_formatter

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import com.pet.vpn_client.core.utils.removeWhiteSpace
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

    fun parseWireguardConfFile(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(EConfigType.WIREGUARD)

        val interfaceParams: MutableMap<String, String> = mutableMapOf()
        val peerParams: MutableMap<String, String> = mutableMapOf()

        var currentSection: String? = null

        str.lines().forEach { line ->
            val trimmedLine = line.trim()

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                return@forEach
            }

            when {
                trimmedLine.startsWith("[Interface]", ignoreCase = true) -> currentSection =
                    "Interface"

                trimmedLine.startsWith("[Peer]", ignoreCase = true) -> currentSection = "Peer"
                else -> {
                    if (currentSection != null) {
                        val parts = trimmedLine.split("=", limit = 2).map { it.trim() }
                        if (parts.size == 2) {
                            val key = parts[0].lowercase()
                            val value = parts[1]
                            when (currentSection) {
                                "Interface" -> interfaceParams[key] = value
                                "Peer" -> peerParams[key] = value
                            }
                        }
                    }
                }
            }
        }

        config.secretKey = interfaceParams["privatekey"].orEmpty()
        config.remarks = System.currentTimeMillis().toString()
        config.localAddress = interfaceParams["address"] ?: Constants.WIREGUARD_LOCAL_ADDRESS_V4
        config.mtu = Utils.parseInt(interfaceParams["mtu"] ?: Constants.WIREGUARD_LOCAL_MTU)
        config.publicKey = peerParams["publickey"].orEmpty()
        config.preSharedKey = peerParams["presharedkey"]?.takeIf { it.isNotEmpty() }
        val endpoint = peerParams["endpoint"].orEmpty()
        val endpointParts = endpoint.split(":", limit = 2)
        if (endpointParts.size == 2) {
            config.server = endpointParts[0]
            config.serverPort = endpointParts[1]
        } else {
            config.server = endpoint
            config.serverPort = ""
        }
        config.reserved = peerParams["reserved"] ?: "0,0,0"

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

    fun toUri(config: ConfigProfileItem): String {
        val dicQuery = HashMap<String, String>()

        dicQuery["publickey"] = config.publicKey.orEmpty()
        if (config.reserved != null) {
            dicQuery["reserved"] = config.reserved.removeWhiteSpace().orEmpty()
        }
        dicQuery["address"] = config.localAddress.removeWhiteSpace().orEmpty()
        if (config.mtu != null) {
            dicQuery["mtu"] = config.mtu.toString()
        }
        if (config.preSharedKey != null) {
            dicQuery["presharedkey"] = config.preSharedKey.removeWhiteSpace().orEmpty()
        }

        return toUri(config, config.secretKey, dicQuery)
    }
}
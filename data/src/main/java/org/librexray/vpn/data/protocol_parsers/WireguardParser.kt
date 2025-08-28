package org.librexray.vpn.data.protocol_parsers

import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

/**
 * Parses WireGuard configuration strings.
 *
 * Supported format (typical):
 * - `wireguard://<privateKey>@host:port?address=...&publickey=...&presharedkey=...&mtu=...&reserved=...#remarks`
 */
class WireguardParser @Inject constructor() : BaseParser() {
    /**
     * Parses a `wireguard://` string and returns a [ConfigProfileItem] on success.
     */
    fun parse(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(ConfigType.WIREGUARD)

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
        config.mtu = (queryParam[QUERY_MTU] ?: WIREGUARD_LOCAL_MTU).toIntOrNull() ?: 0
        config.reserved = queryParam[QUERY_RESERVED] ?: DEFAULT_RESERVED

        return config
    }

    companion object {
        private const val QUERY_ADDRESS = "address"
        private const val QUERY_PUBLIC_KEY = "publickey"
        private const val QUERY_PRESHARED_KEY = "presharedkey"
        private const val QUERY_MTU = "mtu"
        private const val QUERY_RESERVED = "reserved"
        private const val DEFAULT_RESERVED = "0,0,0"
        private const val WIREGUARD_LOCAL_MTU = "1420"
    }
}
package org.librexray.vpn.data.protocol_parsers

import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

/**
 * Parses SOCKS configuration strings.
 * Supported format:
 * - `socks://[username:password@]host:port#remarks`
 */
class SocksParser @Inject constructor() : BaseParser() {
    /**
     * Parses a `socks://` string and returns a [ConfigProfileItem] on success.
     */
    fun parse(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(ConfigType.SOCKS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        if (uri.userInfo?.isEmpty() == false) {
            val result = Utils.decode(uri.userInfo).split(":", limit = 2)
            if (result.count() == 2) {
                config.username = result.first()
                config.password = result.last()
            }
        }

        return config
    }
}
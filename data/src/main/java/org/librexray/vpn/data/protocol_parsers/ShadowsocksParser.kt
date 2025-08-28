package org.librexray.vpn.data.protocol_parsers

import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.domain.models.NetworkType
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

/**
 * Parses Shadowsocks subscription strings.
 *
 * Supported formats:
 * - SIP002;
 * - Legacy: older encoded formats that are still present in the wild.
 */
class ShadowsocksParser @Inject constructor() : BaseParser() {
    /**
     * Parses a Shadowsocks string trying SIP002 first, then legacy format.
     */
    fun parse(str: String): ConfigProfileItem? {
        return parseSip002(str) ?: parseLegacy(str)
    }

    /**
     * Parses SIP002 format: `ss://method:password@host:port#remarks`
     */
    private fun parseSip002(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(ConfigType.SHADOWSOCKS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null
        if (uri.userInfo.isNullOrEmpty()) return null

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        val result = if (uri.userInfo.contains(":")) {
            uri.userInfo.split(":", limit = 2)
        } else {
            Utils.decode(uri.userInfo).split(":", limit = 2)
        }
        if (result.count() == 2) {
            config.method = result.first()
            config.password = result.last()
        }

        if (!uri.rawQuery.isNullOrEmpty()) {
            val queryParam = getQueryParam(uri)
            if (queryParam[QUERY_PLUGIN]?.contains(OBFS_HTTP) == true) {
                val queryPairs = HashMap<String, String>()
                for (pair in queryParam[QUERY_PLUGIN]?.split(";") ?: listOf()) {
                    val idx = pair.split("=")
                    if (idx.count() == 2) {
                        queryPairs[idx.first()] = idx.last()
                    }
                }
                config.network = NetworkType.TCP.type
                config.headerType = HEADER_TYPE_HTTP
                config.host = queryPairs[OBFS_HOST]
                config.path = queryPairs[PATH]
            }
        }

        return config
    }

    /**
     * Parses legacy Shadowsocks format.
     */
    private fun parseLegacy(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(ConfigType.SHADOWSOCKS)
        var result = str.replace(ConfigType.SHADOWSOCKS.protocolScheme, "")
        val indexSplit = result.indexOf("#")
        if (indexSplit > 0) {
            try {
                config.remarks =
                    Utils.urlDecode(result.substring(indexSplit + 1, result.length))
            } catch (e: Exception) {
                Utils.error("Failed to decode remarks in SS legacy URL", e)
            }

            result = result.substring(0, indexSplit)
        }

        val indexS = result.indexOf("@")
        result = if (indexS > 0) {
            Utils.decode(result.substring(0, indexS)) + result.substring(
                indexS,
                result.length
            )
        } else {
            Utils.decode(result)
        }

        val legacyPattern = "^(.+?):(.*)@(.+?):(\\d+?)/?$".toRegex()
        val match = legacyPattern.matchEntire(result) ?: return null

        config.server = match.groupValues[3].removeSurrounding("[", "]")
        config.serverPort = match.groupValues[4]
        config.password = match.groupValues[2]
        config.method = match.groupValues[1].lowercase()

        return config
    }

    companion object {
        private const val QUERY_PLUGIN = "plugin"
        private const val OBFS_HTTP = "obfs=http"
        private const val OBFS_HOST = "obfs-host"
        private const val PATH = "path"
        private const val HEADER_TYPE_HTTP = "http"
    }
}
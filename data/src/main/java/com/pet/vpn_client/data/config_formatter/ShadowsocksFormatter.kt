package com.pet.vpn_client.data.config_formatter

import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class ShadowsocksFormatter @Inject constructor(private val configRepository: Provider<ConfigRepository>) :
    BaseFormatter() {
    fun parse(str: String): ConfigProfileItem? {
        return parseSip002(str) ?: parseLegacy(str)
    }

    fun parseSip002(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(EConfigType.SHADOWSOCKS)

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

    fun parseLegacy(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(EConfigType.SHADOWSOCKS)
        var result = str.replace(EConfigType.SHADOWSOCKS.protocolScheme, "")
        val indexSplit = result.indexOf("#")
        if (indexSplit > 0) {
            try {
                config.remarks =
                    Utils.urlDecode(result.substring(indexSplit + 1, result.length))
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to decode remarks in SS legacy URL", e)
            }

            result = result.substring(0, indexSplit)
        }

        //part decode
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

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configRepository.get().createInitOutbound(EConfigType.SHADOWSOCKS)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            server.password = profileItem.password
            server.method = profileItem.method
        }

        val sni = outboundBean?.streamSettings?.let {
            configRepository.get().populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            configRepository.get().populateTlsSettings(it, profileItem, sni)
        }

        return outboundBean
    }

    companion object {
        private const val QUERY_PLUGIN = "plugin"
        private const val OBFS_HTTP = "obfs=http"
        private const val OBFS_HOST = "obfs-host"
        private const val PATH = "path"
        private const val HEADER_TYPE_HTTP = "http"
    }
}
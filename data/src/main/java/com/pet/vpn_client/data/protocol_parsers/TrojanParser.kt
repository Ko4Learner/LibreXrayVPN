package com.pet.vpn_client.data.protocol_parsers

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

/**
 * Parses Trojan configuration strings.
 *
 * Supported format:
 * - `trojan://password@host:port?\[query]#remarks`
 */
class TrojanParser @Inject constructor() : BaseParser() {
    /**
     * Parses a `trojan://` string and returns a [ConfigProfileItem] on success.
     */
    fun parse(str: String): ConfigProfileItem? {
        val allowInsecure = false
        val config = ConfigProfileItem.create(ConfigType.TROJAN)

        val uri = URI(Utils.fixIllegalUrl(str))
        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo

        if (uri.rawQuery.isNullOrEmpty()) {
            config.network = NetworkType.TCP.type
            config.security = Constants.TLS
            config.insecure = allowInsecure
        } else {
            val queryParam = getQueryParam(uri)

            getItemFromQuery(config, queryParam, allowInsecure)
            config.security = queryParam[SECURITY] ?: Constants.TLS
        }
        return config
    }

    companion object {
        private const val SECURITY = "security"
    }
}
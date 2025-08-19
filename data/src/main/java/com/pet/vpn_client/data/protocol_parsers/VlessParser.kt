package com.pet.vpn_client.data.protocol_parsers

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

/**
 * Parses VLESS configuration strings.
 *
 * Supported format (typical):
 * - `vless://<uuid>@host:port?encryption=none&...#remarks`
 */
class VlessParser @Inject constructor() : BaseParser() {
    /**
     * Parses a `vless://` string and returns a [ConfigProfileItem] on success.
     */
    fun parse(str: String): ConfigProfileItem? {
        val allowInsecure = false
        val config = ConfigProfileItem.create(ConfigType.VLESS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo
        config.method = queryParam[ENCRYPTION] ?: NONE

        getItemFromQuery(config, queryParam, allowInsecure)

        return config
    }

    companion object {
        private const val ENCRYPTION = "encryption"
        private const val NONE = "none"
    }
}
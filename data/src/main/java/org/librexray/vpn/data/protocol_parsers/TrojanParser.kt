/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.data.protocol_parsers

import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.domain.models.NetworkType
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.core.utils.idnHost
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
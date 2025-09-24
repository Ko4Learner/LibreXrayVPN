/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.data.protocol_parsers

import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.coreandroid.utils.Utils
import org.librexray.vpn.coreandroid.utils.idnHost
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
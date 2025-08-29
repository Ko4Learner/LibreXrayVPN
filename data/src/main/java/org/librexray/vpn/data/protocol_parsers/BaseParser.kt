/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.data.protocol_parsers

import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.NetworkType
import org.librexray.vpn.core.utils.Utils
import java.net.URI
import kotlin.text.orEmpty

/**
 * Base class for protocol parsers.
 *
 * Purpose:
 * - Provides common helpers to extract and apply transport/TLS parameters
 *   from a URI query string to a [ConfigProfileItem].
 */
open class BaseParser {
    /**
     * Parses the URI query into a key/value map.
     * @return Map of decoded query parameters.
     */
    fun getQueryParam(uri: URI): Map<String, String> {
        return uri.rawQuery.split("&")
            .associate { it.split("=").let { (k, v) -> k to Utils.urlDecode(v) } }
    }

    /**
     * Applies shared transport/TLS parameters from [queryParam] to [config].
     */
    fun getItemFromQuery(
        config: ConfigProfileItem,
        queryParam: Map<String, String>,
        allowInsecure: Boolean
    ) {
        config.network = queryParam[TYPE] ?: NetworkType.TCP.type
        config.headerType = queryParam[HEADER_TYPE]
        config.host = queryParam[HOST]
        config.path = queryParam[PATH]
        config.seed = queryParam[SEED]
        config.quicSecurity = queryParam[QUIC_SECURITY]
        config.quicKey = queryParam[KEY]
        config.mode = queryParam[MODE]
        config.serviceName = queryParam[SERVICE_NAME]
        config.authority = queryParam[AUTHORITY]
        config.xhttpMode = queryParam[MODE]
        config.xhttpExtra = queryParam[EXTRA]
        config.security = queryParam[SECURITY]
        if (config.security != Constants.TLS && config.security != Constants.REALITY) {
            config.security = null
        }
        config.insecure = if (queryParam[ALLOW_INSECURE].isNullOrEmpty()) {
            allowInsecure
        } else {
                queryParam[ALLOW_INSECURE].orEmpty() == "1"
        }
        config.sni = queryParam[SNI]
        config.fingerPrint = queryParam[FINGERPRINT]
        config.alpn = queryParam[ALPN]
        config.publicKey = queryParam[PUBLIC_KEY]
        config.shortId = queryParam[SHORT_ID]
        config.spiderX = queryParam[SPIDER_X]
        config.flow = queryParam[FLOW]
    }

    companion object {
        private const val TYPE = "type"
        private const val HEADER_TYPE = "headerType"
        private const val HOST = "host"
        private const val PATH = "path"
        private const val SEED = "seed"
        private const val QUIC_SECURITY = "quicSecurity"
        private const val KEY = "key"
        private const val MODE = "mode"
        private const val SERVICE_NAME = "serviceName"
        private const val AUTHORITY = "authority"
        private const val EXTRA = "extra"
        private const val SECURITY = "security"
        private const val ALLOW_INSECURE = "allowInsecure"
        private const val SNI = "sni"
        private const val FINGERPRINT = "fp"
        private const val ALPN = "alpn"
        private const val PUBLIC_KEY = "pbk"
        private const val SHORT_ID = "sid"
        private const val SPIDER_X = "spx"
        private const val FLOW = "flow"
    }
}
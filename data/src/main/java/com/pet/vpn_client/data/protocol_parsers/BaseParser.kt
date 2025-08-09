package com.pet.vpn_client.data.protocol_parsers

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.Utils
import java.net.URI
import kotlin.text.orEmpty

open class BaseParser {
    fun getQueryParam(uri: URI): Map<String, String> {
        return uri.rawQuery.split("&")
            .associate { it.split("=").let { (k, v) -> k to Utils.urlDecode(v) } }
    }

    fun getItemFormQuery(
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
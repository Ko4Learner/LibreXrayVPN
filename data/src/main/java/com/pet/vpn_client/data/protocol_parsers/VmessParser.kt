package com.pet.vpn_client.data.protocol_parsers

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

/**
 * Parses VMess configuration strings in two known formats:
 *
 * 1) **Base64 JSON (VMess QR code)**:
 *    `vmess://<base64-encoded JSON>`
 *    The JSON typically contains: `add` (host), `port`, `id` (UUID), `net`, `type`, `host`, `path`,
 *    `scy` (security), `tls`, `sni`, `alpn`, `fp`, etc.
 *
 * 2) **URI-style (VMess-URI)**:
 *    `vmess://<uuid>@host:port?[query]#remarks`
 *    Transport/TLS details are provided via query parameters and applied through [BaseParser].
 */
class VmessParser @Inject constructor(private val gson: Gson) : BaseParser() {
    /**
     * Parses a VMess string:
     * - URI-style if the heuristic matches (contains both `'?'` and `'&'`),
     * - otherwise Base64-decoded JSON.
     */
    fun parse(str: String): ConfigProfileItem? {
        if (str.indexOf('?') > 0 && str.indexOf('&') > 0) {
            return parseVmessStd(str)
        }

        val allowInsecure = false
        val config = ConfigProfileItem.create(ConfigType.VMESS)

        var result = str.replace(ConfigType.VMESS.protocolScheme, "")
        result = Utils.decode(result)
        if (TextUtils.isEmpty(result)) {
            Log.w(Constants.TAG, "Base64 decoding failed")
            return null
        }
        val vmessQRCode = gson.fromJson(result, VmessQRCode::class.java)
        if (TextUtils.isEmpty(vmessQRCode.add)
            || TextUtils.isEmpty(vmessQRCode.port)
            || TextUtils.isEmpty(vmessQRCode.id)
            || TextUtils.isEmpty(vmessQRCode.net)
        ) {
            Log.w(Constants.TAG, "Incorrect protocol")
            return null
        }

        config.remarks = vmessQRCode.ps
        config.server = vmessQRCode.add
        config.serverPort = vmessQRCode.port
        config.password = vmessQRCode.id
        config.method =
            if (TextUtils.isEmpty(vmessQRCode.scy)) DEFAULT_SECURITY else vmessQRCode.scy

        config.network = vmessQRCode.net.ifEmpty { NetworkType.TCP.type }
        config.headerType = vmessQRCode.type
        config.host = vmessQRCode.host
        config.path = vmessQRCode.path

        when (NetworkType.fromString(config.network)) {
            NetworkType.KCP -> {
                config.seed = vmessQRCode.path
            }

            NetworkType.GRPC -> {
                config.mode = vmessQRCode.type
                config.serviceName = vmessQRCode.path
                config.authority = vmessQRCode.host
            }

            else -> {}
        }

        config.security = vmessQRCode.tls
        config.insecure = allowInsecure
        config.sni = vmessQRCode.sni
        config.fingerPrint = vmessQRCode.fp
        config.alpn = vmessQRCode.alpn

        return config
    }

    /**
     * Parses the URI-style VMess format: `vmess://<uuid>@host:port?\[query]#remarks`
     */
    private fun parseVmessStd(str: String): ConfigProfileItem? {
        val allowInsecure = false
        val config = ConfigProfileItem.create(ConfigType.VMESS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo
        config.method = DEFAULT_SECURITY

        getItemFromQuery(config, queryParam, allowInsecure)

        return config
    }

    companion object {
        private const val DEFAULT_SECURITY = "auto"
    }
}

/**
 * Payload model for Base64-encoded VMess QR JSON.
 * Only fields used by the parser are included.
 */
private data class VmessQRCode(
    var v: String = "",
    var ps: String = "",
    var add: String = "",
    var port: String = "",
    var id: String = "",
    var aid: String = "0",
    var scy: String = "",
    var net: String = "",
    var type: String = "",
    var host: String = "",
    var path: String = "",
    var tls: String = "",
    var sni: String = "",
    var alpn: String = "",
    var fp: String = ""
)
package com.pet.vpn_client.data

import java.util.UUID
import androidx.core.net.toUri
import com.pet.vpn_client.data.dto.VlessConfig


fun parseVlessUrl(url: String): VlessConfig? {
    return try {
        val uri = url.toUri()

        if (uri.scheme != "vless") {
            return null
        }

        val userInfo = uri.userInfo?.split(":")
        val uuid = userInfo?.get(0)?.let { UUID.fromString(it) } ?: return null

        val address = uri.host ?: return null
        val port = uri.port.takeIf { it != -1 } ?: return null

        val params = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) }

        VlessConfig(
            address = address,
            port = port,
            uuid = uuid,
            encryption = params["encryption"] ?: "none",
            security = params["security"] ?: "none",
            sni = params["sni"],
            flow = params["flow"],
            fingerprint = params["fp"],
            alpn = params["alpn"],
            type = params["type"] ?: "tcp",
            headerType = params["headerType"] ?: "none",
            host = params["host"],
            path = params["path"],
            serviceName = params["serviceName"],
            streamSecurity = params["streamSecurity"],
            tag = uri.fragment,
            transportProtocol = params["tp"],
        )
    } catch (e: Exception) {
        // Handle parsing errors (e.g., invalid UUID format, malformed URL)
        e.printStackTrace()
        null
    }
}

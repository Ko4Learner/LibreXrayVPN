package com.pet.vpn_client.data.dto

import java.util.UUID

data class VlessConfig(
    val address: String,
    val port: Int,
    val uuid: UUID,
    val encryption: String = "none",
    val security: String = "none",
    val sni: String? = null,
    val flow: String? = null,
    val fingerprint: String? = null,
    val alpn: String? = null,
    val type: String = "tcp",
    val headerType: String = "none",
    val host: String? = null,
    val path: String? = null,
    val serviceName: String? = null,
    val streamSecurity: String? = null,
    val tag: String? = null,
    val transportProtocol: String? = null,
)
package com.pet.vpn_client.domain.models

data class ConfigResult(
    var status: Boolean,
    var guid: String? = null,
    var content: String = "",
    var socksPort: Int? = null,
)
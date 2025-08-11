package com.pet.vpn_client.domain.models

/**
 * Represents the result of retrieving or generating a core configuration.
 */
data class ConfigResult(
    var status: Boolean,
    var guid: String? = null,
    var content: String = "",
    var socksPort: Int? = null,
)
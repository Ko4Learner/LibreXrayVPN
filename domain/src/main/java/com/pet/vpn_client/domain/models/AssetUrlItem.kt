package com.pet.vpn_client.domain.models

data class AssetUrlItem(
    var remarks: String = "",
    var url: String = "",
    val addedTime: Long = System.currentTimeMillis(),
    var lastUpdated: Long = -1,
    var locked: Boolean? = false,
)
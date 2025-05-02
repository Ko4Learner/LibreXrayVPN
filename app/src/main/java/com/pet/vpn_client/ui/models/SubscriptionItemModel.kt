package com.pet.vpn_client.ui.models

data class SubscriptionItemModel(
    val id: Int = 0,
    val imageCountryId: Int,
    val name: String,
    val ip: String,
    val protocol: String,
    val description: String,
    val isSelected: Boolean = false
)

package com.pet.vpn_client.presentation.models

data class SubscriptionItemModel(
    val id: String = "",
    val name: String,
    val ip: String,
    val protocol: String,
    val isSelected: Boolean = false
)

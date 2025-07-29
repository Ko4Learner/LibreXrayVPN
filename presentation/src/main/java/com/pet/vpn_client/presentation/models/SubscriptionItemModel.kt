package com.pet.vpn_client.presentation.models

data class SubscriptionItemModel(
    val id: String = "",
//    val imageCountryId: Int,
    val name: String,
    val ip: String,
    val protocol: String,
//    val description: String,
    val isSelected: Boolean = false
)

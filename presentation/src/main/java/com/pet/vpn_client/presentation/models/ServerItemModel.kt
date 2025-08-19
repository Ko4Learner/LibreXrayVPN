package com.pet.vpn_client.presentation.models

data class ServerItemModel(
    val guid: String = "",
    val name: String,
    val ip: String,
    val protocol: String,
    val isSelected: Boolean = false
)


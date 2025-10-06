package org.librexray.vpn.presentation.model

data class ServerItemModel(
    val guid: String = "",
    val name: String,
    val ip: String,
    val protocol: String,
    val isSelected: Boolean = false
)
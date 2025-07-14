package com.pet.vpn_client.presentation.navigation


sealed class NavItem(val route: String) {
    object VpnScreen : NavItem("Main")
    object QrCodeScreen : NavItem("QrCode")
}
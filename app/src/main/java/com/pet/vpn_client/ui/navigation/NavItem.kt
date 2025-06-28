package com.pet.vpn_client.ui.navigation


sealed class NavItem(val route: String) {
    object VpnScreen : NavItem("Main")
    object QrCodeScreen : NavItem("Settings")
}
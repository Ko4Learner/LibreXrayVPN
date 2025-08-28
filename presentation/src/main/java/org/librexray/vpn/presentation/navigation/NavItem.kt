package org.librexray.vpn.presentation.navigation


sealed class NavItem(val route: String) {
    object VpnScreen : NavItem("Main")
    object QrCodeScreen : NavItem("QrCode")
}
package org.librexray.vpn.presentation.navigation

/**
 * Centralized definition of all navigation routes used in the app.
 *
 * Each destination exposes a stable [route] constant.
 */
sealed class NavItem(val route: String) {

    /** Main VPN control screen. Start destination. */
    data object VpnScreen : NavItem(Route.MAIN)

    /** QR scanner screen to import server configurations. */
    data object QrCodeScreen : NavItem(Route.QR_CODE)

    /** App-level settings screen. */
    data object SettingsScreen : NavItem(Route.SETTINGS)

    /**
     * Centralized collection of route and result keys used in navigation.
     */
    object Route {
        /** Route for the main VPN screen. */
        const val MAIN: String = "Main"

        /** Route for the QR code scanner screen. */
        const val QR_CODE: String = "QrCode"

        /** Route for the settings screen. */
        const val SETTINGS: String = "Settings"

        /** Result key: set to true in SavedStateHandle when QR import succeeds. */
        const val QR_CODE_IMPORTED_KEY: String = "qrCodeImported"
    }
}
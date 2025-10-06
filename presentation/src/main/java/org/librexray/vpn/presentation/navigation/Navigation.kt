package org.librexray.vpn.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.librexray.vpn.presentation.navigation.NavItem.Route.QR_CODE_IMPORTED_KEY
import org.librexray.vpn.presentation.screens.QrCodeScreen
import org.librexray.vpn.presentation.screens.SettingsScreen
import org.librexray.vpn.presentation.screens.VpnScreen

/**
 * Configures the app's navigation graph.
 *
 * Responsibilities:
 * - Declares start destination [NavItem.VpnScreen].
 * - Wires callbacks for navigation between VPN, QR, and Settings screens.
 * - Uses [NavItem.Route.QR_CODE_IMPORTED_KEY] to communicate one-off results
 *   from the QR screen back to the VPN screen.
 */
@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState
) {
    NavHost(navController, startDestination = NavItem.VpnScreen.route) {
        composable(NavItem.VpnScreen.route) {
            VpnScreen(
                Modifier.padding(innerPadding),
                navController,
                onQrCodeClick = { navController.navigate(NavItem.QrCodeScreen.route) },
                onSettingsClick = { navController.navigate(NavItem.SettingsScreen.route) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(NavItem.QrCodeScreen.route) {
            QrCodeScreen(
                Modifier.padding(innerPadding),
                onBackClick = { navController.popBackStack() },
                onResult = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        QR_CODE_IMPORTED_KEY,
                        true
                    )
                    navController.popBackStack()
                }
            )
        }
        composable(NavItem.SettingsScreen.route) {
            SettingsScreen(
                Modifier.padding(innerPadding),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
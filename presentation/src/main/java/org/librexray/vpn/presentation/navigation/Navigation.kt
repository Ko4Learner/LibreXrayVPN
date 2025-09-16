package org.librexray.vpn.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.librexray.vpn.presentation.screens.QrCodeScreen
import org.librexray.vpn.presentation.screens.SettingsScreen
import org.librexray.vpn.presentation.screens.VpnScreen

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    getString: (Int) -> String
) {
    NavHost(navController, startDestination = NavItem.VpnScreen.route) {
        composable(NavItem.VpnScreen.route) {
            VpnScreen(
                Modifier.padding(innerPadding),
                navController,
                onQrCodeClick = { navController.navigate(NavItem.QrCodeScreen.route) },
                onSettingsClick = { navController.navigate(NavItem.SettingsScreen.route) },
                getString = getString
            )
        }

        composable(NavItem.QrCodeScreen.route) {
            QrCodeScreen(
                Modifier.padding(innerPadding),
                onBackClick = { navController.popBackStack() },
                onResult = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "qrCodeImported",
                        true
                    )
                    navController.popBackStack()
                },
                getString = getString
            )
        }
        composable(NavItem.SettingsScreen.route) {
            SettingsScreen(
                Modifier.padding(innerPadding),
                onBackClick = { navController.popBackStack() },
                getString = getString
            )
        }
    }
}
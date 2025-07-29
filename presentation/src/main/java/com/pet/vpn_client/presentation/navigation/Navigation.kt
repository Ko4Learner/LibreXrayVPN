package com.pet.vpn_client.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pet.vpn_client.presentation.screens.QrCodeScreen
import com.pet.vpn_client.presentation.screens.VpnScreen

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(navController, startDestination = NavItem.VpnScreen.route) {
        composable(NavItem.VpnScreen.route) {
            VpnScreen(
                Modifier.padding(innerPadding),
                navController,
                onQrCodeClick = { navController.navigate(NavItem.QrCodeScreen.route) })
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
                })
        }
    }
}
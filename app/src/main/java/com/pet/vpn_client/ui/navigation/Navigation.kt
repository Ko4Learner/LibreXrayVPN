package com.pet.vpn_client.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pet.vpn_client.ui.screens.QrCodeScreen
import com.pet.vpn_client.ui.screens.VpnScreen

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    NavHost(navController, startDestination = NavItem.VpnScreen.route) {
        composable(NavItem.VpnScreen.route) {
            VpnScreen(
                Modifier.padding(innerPadding),
                onQrCodeClick = { navController.navigate(NavItem.QrCodeScreen.route) })
        }

        composable(NavItem.QrCodeScreen.route) {
            QrCodeScreen(
                Modifier.padding(innerPadding),
                onResult = {
                    navController.popBackStack()
                })
        }
    }
}
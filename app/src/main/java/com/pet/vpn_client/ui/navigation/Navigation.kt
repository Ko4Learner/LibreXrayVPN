package com.pet.vpn_client.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pet.vpn_client.ui.screens.MainScreen
import com.pet.vpn_client.ui.screens.SettingsScreen

@Composable
fun Navigation(
    navController: NavHostController,
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    NavHost(navController, startDestination = BottomNavItem.Main.route) {
        composable(BottomNavItem.Main.route) {
            MainScreen(Modifier.padding(innerPadding))
        }

        composable(BottomNavItem.Settings.route) {
            SettingsScreen(Modifier.padding(innerPadding))
        }
    }
}
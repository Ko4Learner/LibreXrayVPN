package com.pet.vpn_client.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Text(text = "Main Screen", modifier = modifier)
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Text(text = "Settings Screen", modifier = modifier)
}
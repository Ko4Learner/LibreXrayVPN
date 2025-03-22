package com.pet.vpn_client.ui.navigation

import com.pet.vpn_client.R

sealed class BottomNavItem(val title: String, val route: String, val icon: Int) {
    object Main : BottomNavItem("Main", "main", R.drawable.ic_list)
    object Settings : BottomNavItem("Settings", "settings", R.drawable.ic_settings)
}
package com.pet.vpn_client.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.view_model.SettingsViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    Text(text = "Settings Screen", modifier = modifier)
}
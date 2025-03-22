package com.pet.vpn_client.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: VpnScreenViewModel = hiltViewModel()
    Text(text = "Main Screen", modifier = modifier)
}
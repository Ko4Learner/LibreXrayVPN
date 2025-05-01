package com.pet.vpn_client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: VpnScreenViewModel = hiltViewModel()
    Column(modifier = modifier) {
        StartVpnButton(viewModel)
    }
}

@Composable
fun StartVpnButton(viewModel: VpnScreenViewModel) {

    val color = remember {
        mutableStateOf(Color.Red)
    }
    Box(
        modifier = Modifier
            .background(color = color.value, shape = CircleShape)
            .size(200.dp)
            .clickable {
                //viewModel.StartStopVPN()
            },
        contentAlignment = Alignment.Center

    ) {

    }
}
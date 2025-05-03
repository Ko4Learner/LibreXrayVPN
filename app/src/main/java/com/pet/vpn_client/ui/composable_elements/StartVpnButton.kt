package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel

@Composable
fun StartVpnButton(viewModel: VpnScreenViewModel) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(color = colorScheme.secondary, shape = CircleShape)
            .size(80.dp)
            .clickable {
                viewModel.toggleVpnProxy()
            },
        contentAlignment = Alignment.Center

    ) {
        Text(
            text = "Start",
            color = colorScheme.onSecondary,
            style = MaterialTheme.typography.titleMedium
        )
        //TODO Add VPN icon
    }
}
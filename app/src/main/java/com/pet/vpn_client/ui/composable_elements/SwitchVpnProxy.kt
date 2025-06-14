package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pet.vpn_client.presentation.intent.VpnScreenIntent
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel

@Composable
fun SwitchVpnProxy(viewModel: VpnScreenViewModel) {
    var isChecked by remember {
        mutableStateOf(false)
    }
    val text by remember {
        derivedStateOf {
            if (isChecked) "VPN" else "Proxy"
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                viewModel.onIntent(VpnScreenIntent.SwitchVpnProxy)
            },
            colors = SwitchDefaults.colors()
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
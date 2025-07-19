package com.pet.vpn_client.presentation.composable_elements

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

@Composable
fun SwitchVpnProxy(onIntent: (VpnScreenIntent) -> Unit, isVpnMode: Boolean) {
    val text by remember {
        derivedStateOf {
            if (isVpnMode) "VPN" else "Proxy"
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(
            checked = isVpnMode,
            onCheckedChange = {
                onIntent(VpnScreenIntent.SwitchVpnProxy)
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
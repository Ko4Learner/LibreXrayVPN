package com.pet.vpn_client.presentation.composable_elements

import android.app.Activity
import android.net.VpnService
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.presentation.intent.VpnScreenIntent

@Composable
fun StartVpnButton(onIntent: (VpnScreenIntent) -> Unit, isRunning: Boolean) {
    val context = LocalContext.current
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onIntent(VpnScreenIntent.ToggleConnection)
        } else {
            Log.d(Constants.TAG, "Permission denied")
        }
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(color = colorScheme.secondary, shape = CircleShape)
            .size(80.dp)
            .clickable {
                val intent = VpnService.prepare(context)
                if (intent == null) {
                    onIntent(VpnScreenIntent.ToggleConnection)
                } else {
                    vpnPermissionLauncher.launch(intent)
                }
            },
        contentAlignment = Alignment.Center

    ) {
        Text(
            text = if (!isRunning) "Start" else "Stop",
            color = colorScheme.onSecondary,
            style = MaterialTheme.typography.titleMedium
        )
        //TODO Add VPN icon
    }
}
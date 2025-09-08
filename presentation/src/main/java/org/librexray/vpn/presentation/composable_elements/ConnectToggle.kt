package org.librexray.vpn.presentation.composable_elements

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
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.intent.VpnScreenIntent

@Composable
fun ConnectToggle(
    onIntent: (VpnScreenIntent) -> Unit, isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onIntent(VpnScreenIntent.ToggleConnection)
        } else {
            Log.d(Constants.TAG, "VPN permission denied")
        }
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .background(
                color = if (!isRunning) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
                shape = CircleShape
            )
            .size(160.dp)
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
        Icon(
            imageVector = if (!isRunning) AppIcons.Start else AppIcons.Stop,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface,
            modifier = Modifier.size(64.dp)
        )
    }
}
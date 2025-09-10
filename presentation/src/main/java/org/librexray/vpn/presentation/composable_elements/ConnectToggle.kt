package org.librexray.vpn.presentation.composable_elements

import android.app.Activity
import android.net.VpnService
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            .size(160.dp)
            .clip(CircleShape)
            .background(
                brush = if (isRunning)
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF06B9FF),
                            0.28f to Color(0xFF17C3FF),
                            0.54f to Color(0xFF2F88FF),
                            0.74f to Color(0xFF4B64FF),
                            0.88f to Color(0x88515EFF),
                            1.00f to Color(0x00515EFF)
                        )
                    )
                else
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF3F464D),
                            0.35f to Color(0xFF444B52),
                            0.65f to Color(0xFF393F45),
                            0.85f to Color(0x663F464D),
                            1.00f to Color(0x003F464D)
                        )
                    )
            )
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
            imageVector = AppIcons.Toggle,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface,
            modifier = Modifier.size(54.dp)
        )
    }
}
package org.librexray.vpn.presentation.composable_elements

import android.app.Activity
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
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
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.intent.VpnScreenIntent

@Composable
fun ConnectToggle(
    modifier: Modifier = Modifier,
    onIntent: (VpnScreenIntent) -> Unit,
    isRunning: Boolean,
    emptyServerList: Boolean,
    showBottomSheet: () -> Unit
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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { result ->
        val intent = VpnService.prepare(context)
        if (intent == null) {
            onIntent(VpnScreenIntent.ToggleConnection)
        } else {
            vpnPermissionLauncher.launch(intent)
        }
        if (!result) {
            Log.d(Constants.TAG, "Notification permission denied")
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
                else if (MaterialTheme.colors.isLight) {
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xB35A6B80),
                            0.25f to Color(0x8C5A6B80),
                            0.50f to Color(0x665A6B80),
                            0.75f to Color(0x335A6B80),
                            1.00f to Color(0x005A6B80)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF2F3439),
                            0.30f to Color(0xFF3A4046),
                            0.60f to Color(0xFF2B3035),
                            0.82f to Color(0x662F3439),
                            1.00f to Color(0x002F3439)
                        )
                    )
                }
            )
            .clickable {
                if (emptyServerList) {
                    showBottomSheet()
                    return@clickable
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasNotification = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasNotification) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        return@clickable
                    }
                }

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
            painter = if (emptyServerList) {
                AppIcons.Add.rememberPainter()
            } else {
                AppIcons.Toggle.rememberPainter()
            },
            contentDescription = if (emptyServerList) {
                stringResource(R.string.add_configuration)
            } else if (isRunning) {
                stringResource(R.string.stop_vpn)
            } else {
                stringResource(R.string.start_vpn)
            },
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}
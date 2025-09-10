package org.librexray.vpn.presentation.design_system.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.ui.graphics.vector.ImageVector
import org.librexray.vpn.core.R

/**
 * Centralized registry of app icons.
 *
 * All icons used across the VPN client are referenced here
 * with semantic names instead of calling Icons.* directly.
 */
object AppIcons {
    val QrScan: ImageVector = Icons.Outlined.QrCode
    val Clipboard: ImageVector = Icons.Filled.ContentPaste
    val Toggle: ImageVector = Icons.Filled.PowerSettingsNew
    val Delete: ImageVector = Icons.Filled.Delete
    val Add: ImageVector = Icons.Filled.Add
    val Close: ImageVector = Icons.Filled.Close
    val arrowForward: ImageVector = Icons.Default.ChevronRight
    val LatencyTest: Int = R.drawable.vector_test_connection
}
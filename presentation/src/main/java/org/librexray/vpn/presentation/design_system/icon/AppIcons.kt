package org.librexray.vpn.presentation.design_system.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized registry of app icons.
 *
 * All icons used across the VPN client are referenced here
 * with semantic names instead of calling Icons.* directly.
 */
object AppIcons {
    val Menu: ImageVector = Icons.Outlined.MoreVert
    val QrScan: ImageVector = Icons.Outlined.QrCode
    val Clipboard: ImageVector = Icons.Filled.ContentPaste
    val Start: ImageVector = Icons.Filled.PlayArrow
    val Stop: ImageVector = Icons.Filled.Stop
    val Delete: ImageVector = Icons.Filled.Delete
}
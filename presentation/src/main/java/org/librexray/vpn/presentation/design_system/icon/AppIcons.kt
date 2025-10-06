package org.librexray.vpn.presentation.design_system.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import org.librexray.vpn.coreandroid.R

/**
 * Centralized registry of all app icons.
 *
 * Provides stable, semantic references to icons used across the VPN client.
 *
 * Adding a new icon:
 * - Define it here with a descriptive name.
 * - Prefer [IconType.Vector] when using Material icons,
 *   or [IconType.Drawable] for custom resources.
 */
object AppIcons {
    val QrScan = IconType.Vector(Icons.Outlined.QrCodeScanner)
    val Clipboard = IconType.Vector(Icons.Filled.ContentCopy)
    val Toggle = IconType.Vector(Icons.Filled.PowerSettingsNew)
    val Delete = IconType.Vector(Icons.Outlined.Delete)
    val Add = IconType.Vector(Icons.Filled.Add)
    val Close = IconType.Vector(Icons.Filled.Close)
    val ArrowForward = IconType.Vector(Icons.Default.ChevronRight)
    val ArrowBack = IconType.Vector(Icons.Default.ChevronLeft)
    val Menu = IconType.Vector(Icons.Outlined.Settings)
    val Theme = IconType.Vector(Icons.Outlined.DarkMode)
    val Language = IconType.Vector(Icons.Outlined.Public)
    val Info = IconType.Vector(Icons.Outlined.Info)

    val Github = IconType.Drawable(R.drawable.vector_github)
    val LatencyTest = IconType.Drawable(R.drawable.vector_test_connection)
    val AppIcon = IconType.Drawable(R.drawable.ic_notification)
}
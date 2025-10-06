package org.librexray.vpn.presentation.model

/**
 * UI-facing model of a VPN server item shown in lists and pickers.
 *
 * Semantics:
 * - [guid] is a stable unique identifier used for diffing and selection.
 * - [name] is a user-readable title (e.g., "Frankfurt-01").
 * - [ip] is a masked endpoint prepared for display (no sensitive data).
 * - [protocol] is a human-readable protocol label (e.g., "VLESS", "VMESS").
 * - [isSelected] marks which server is currently chosen in the UI.
 */
data class ServerItemModel(
    val guid: String,
    val name: String,
    val ip: String,
    val protocol: String,
    val isSelected: Boolean = false
)
package org.librexray.vpn.presentation.mapper

import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.presentation.model.ServerItemModel

/**
 * Maps a [ConfigProfileItem] from the configuration layer
 * to a UI-friendly [ServerItemModel].
 *
 * Behavior:
 * - Masks sensitive parts of the server address using [getAddress].
 * - Includes protocol type and user-readable remarks.
 *
 * @param guid Unique identifier for this configuration profile.
 * @return A presentation-layer model safe to display in lists or cards.
 */
fun ConfigProfileItem.toServerItemModel(guid: String): ServerItemModel {
    return ServerItemModel(
        guid = guid,
        name = remarks,
        ip = getAddress(this),
        protocol = configType.name
    )
}

/**
 * Masks a server's address to hide sensitive segments.
 *
 * Examples:
 * - "192.168.0.101" → "192.168.0.***"
 * @return Masked IP or host with port suffix.
 */
private fun getAddress(profile: ConfigProfileItem): String {
    return "${
        profile.server?.let {
            if (it.contains(":"))
                it.split(":").take(2).joinToString(":", postfix = ":***")
            else
                it.split('.').dropLast(1).joinToString(".", postfix = ".***")
        }
    } : ${profile.serverPort}"
}
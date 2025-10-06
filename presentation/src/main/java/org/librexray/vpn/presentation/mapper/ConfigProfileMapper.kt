package org.librexray.vpn.presentation.mapper

import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.presentation.model.ServerItemModel

fun ConfigProfileItem.toServerItemModel(guid: String): ServerItemModel {
    return ServerItemModel(
        guid = guid,
        name = remarks,
        ip = getAddress(this),
        protocol = configType.name
    )
}

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
package com.pet.vpn_client.presentation.formatter

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.presentation.models.ServerItemModel

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
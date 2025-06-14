package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.presentation.models.ServerItemModel
import com.pet.vpn_client.presentation.models.SubscriptionItemModel

@Composable
fun SubscriptionsList(itemList: List<ServerItemModel>) {
    val list: MutableList<SubscriptionItemModel> = mutableListOf()
    for (item in itemList) {
        list.add(
            SubscriptionItemModel(
                name = item.profile.remarks,
                ip = getAddress(item.profile),
                protocol = item.profile.configType.name,
            )
        )
    }
    LazyColumn {
        itemsIndexed(
            items = list
        )
        { _, item ->
            SubscriptionItem(item = item)
        }
    }
}

//TODO перенести в модель данных
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
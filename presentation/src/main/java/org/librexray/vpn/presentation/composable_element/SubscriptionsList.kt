package org.librexray.vpn.presentation.composable_element

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.librexray.vpn.presentation.composable_element.item.SubscriptionItem
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.model.ServerItemModel

@Composable
fun SubscriptionsList(
    modifier: Modifier = Modifier,
    onIntent: (VpnScreenIntent) -> Unit,
    itemList: List<ServerItemModel>,
    selectedServerId: String?
) {
    val list: MutableList<ServerItemModel> = mutableListOf()
    for (item in itemList) {
        list.add(
            ServerItemModel(
                guid = item.guid,
                name = item.name,
                ip = item.ip,
                protocol = item.protocol,
            )
        )
    }
    LazyColumn(modifier = modifier) {
        itemsIndexed(
            items = list
        )
        { _, item ->
            SubscriptionItem(
                item = item,
                selectedServerId = selectedServerId,
                buttonIcon = AppIcons.Delete,
                onButtonClick = { onIntent(VpnScreenIntent.DeleteItem(it.guid)) },
                onCardClick = { onIntent(VpnScreenIntent.SetSelectedServer(it.guid)) }
            )
        }
    }
}
package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun SubscriptionsList(
    onIntent: (VpnScreenIntent) -> Unit,
    itemList: List<ServerItemModel>,
    showBottomSheet: () -> Unit,
    modifier: Modifier = Modifier
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
    LazyColumn {
        itemsIndexed(
            items = list
        )
        { _, item ->
            SubscriptionItem(onIntent = onIntent, item = item, showBottomSheet = showBottomSheet)
        }
    }
}
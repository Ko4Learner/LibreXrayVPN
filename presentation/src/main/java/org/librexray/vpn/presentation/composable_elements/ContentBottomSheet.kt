package org.librexray.vpn.presentation.composable_elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun ContentBottomSheet(
    onIntent: (VpnScreenIntent) -> Unit,
    itemList: List<ServerItemModel>,
    showBottomSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubscriptionsList(onIntent = onIntent,itemList = itemList, showBottomSheet)
}
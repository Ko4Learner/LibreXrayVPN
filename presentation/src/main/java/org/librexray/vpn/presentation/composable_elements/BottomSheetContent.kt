package org.librexray.vpn.presentation.composable_elements

import androidx.compose.runtime.Composable
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun BottomSheetContent(
    onIntent: (VpnScreenIntent) -> Unit,
    itemList: List<ServerItemModel>,
    showBottomSheet: () -> Unit
) {
    SubscriptionsList(onIntent = onIntent,itemList = itemList, showBottomSheet)
}
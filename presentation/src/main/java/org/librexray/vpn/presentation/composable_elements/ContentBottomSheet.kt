package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun ContentBottomSheet(
    modifier: Modifier = Modifier,
    onIntent: (VpnScreenIntent) -> Unit,
    hideBottomSheet: () -> Unit,
    itemList: List<ServerItemModel>,
    selectedServerId: String?
) {
    val sheetMaxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.7f
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .heightIn(max = sheetMaxHeight)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(50)
                )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Мои конфигурации",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface
            )
            IconButton(onClick = hideBottomSheet) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colors.onSurface
                )
            }

        }
        SubscriptionsList(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            onIntent = onIntent,
            itemList = itemList,
            selectedServerId = selectedServerId
        )
    }
}
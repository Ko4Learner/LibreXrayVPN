package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun SubscriptionItem(
    onIntent: (VpnScreenIntent) -> Unit,
    item: ServerItemModel,
    showBottomSheet: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 24.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        showBottomSheet()
                    },
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = item.ip,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = item.protocol,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = { onIntent(VpnScreenIntent.DeleteItem(item.guid)) }
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}
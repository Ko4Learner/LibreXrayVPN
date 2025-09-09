package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun SubscriptionItem(
    modifier: Modifier = Modifier,
    item: ServerItemModel,
    selectedServerId: String?,
    buttonIcon: ImageVector,
    buttonIntent: (ServerItemModel) -> Unit,
    confirmOnButton: Boolean,
    onCardClick: ((ServerItemModel) -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    val buttonAction = {
        if (confirmOnButton) showDialog = true
        else buttonIntent(item)
    }

    val isSelected = item.guid == selectedServerId
    val cardModifier = modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth()
        .let {
            if (isSelected) it.border(
                width = 1.dp,
                color = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) else it
        }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = if (isSelected) MaterialTheme.colors.surface else MaterialTheme.colors.surface.copy(
            alpha = 0.7f
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onCardClick != null) { onCardClick?.invoke(item) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
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
                modifier = Modifier
                    .size(48.dp),
                onClick = buttonAction
            ) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            title = {
                Text(
                    text = "Удалить элемент?",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
            },
            onDismissRequest = { showDialog = false },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text(
                        text = "Отменить",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    buttonIntent(item)
                    showDialog = false
                }) {
                    Text(
                        text = "Удалить",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            })
    }
}
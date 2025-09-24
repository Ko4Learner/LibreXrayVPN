package org.librexray.vpn.presentation.composable_elements.items

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.presentation.design_system.icon.IconType
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.models.ServerItemModel

@Composable
fun SubscriptionItem(
    modifier: Modifier = Modifier,
    item: ServerItemModel,
    selectedServerId: String?,
    buttonIcon: IconType,
    onButtonClick: ((ServerItemModel) -> Unit)? = null,
    onCardClick: (ServerItemModel) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val cardModifier = modifier
        .padding(vertical = 4.dp)
        .fillMaxWidth()
        .let {
            if (item.guid == selectedServerId) it.border(
                width = 1.dp,
                color = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) else it
        }

    val painter = buttonIcon.rememberPainter()

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick(item) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.size(4.dp))
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
                onClick = {
                    if (onButtonClick != null) showDialog = true
                    else onCardClick(item)
                }
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.delete_item),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.no),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onButtonClick?.invoke(item)
                    showDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.yes),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.surface
        )
    }
}
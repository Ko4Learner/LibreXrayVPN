package org.librexray.vpn.presentation.composable_element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.model.ServerItemModel

@Composable
fun ContentBottomSheet(
    modifier: Modifier = Modifier,
    onIntent: (VpnScreenIntent) -> Unit,
    onQrCodeClick: () -> Unit,
    hideBottomSheet: () -> Unit,
    itemList: List<ServerItemModel>,
    selectedServerId: String?
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
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
                text = if (itemList.isEmpty()) stringResource(R.string.add_configuration)
                else stringResource(R.string.my_configurations),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface
            )
            IconButton(onClick = hideBottomSheet) {
                Icon(
                    painter = AppIcons.Close.rememberPainter(),
                    contentDescription = stringResource(R.string.close),
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
        ImportButtonRow(onIntent = onIntent, onQrCodeClick = onQrCodeClick)
    }
}

@Composable
private fun ImportButtonRow(
    modifier: Modifier = Modifier,
    onIntent: (VpnScreenIntent) -> Unit,
    onQrCodeClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f)
                .height(96.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
            onClick = onQrCodeClick
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = AppIcons.QrScan.rememberPainter(),
                    contentDescription = stringResource(R.string.qr_code),
                    tint = MaterialTheme.colors.onSurface
                )
                Text(
                    text = stringResource(R.string.qr_code),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        Button(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
                .height(96.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
            onClick = { onIntent(VpnScreenIntent.ImportConfigFromClipboard) }
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    painter = AppIcons.Clipboard.rememberPainter(),
                    contentDescription = stringResource(R.string.clipboard),
                    tint = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.clipboard),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}
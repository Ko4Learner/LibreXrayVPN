package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.intent.VpnScreenIntent

@Composable
fun ConfigDropDownMenu(onIntent: (VpnScreenIntent) -> Unit, onQrCodeClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = AppIcons.Menu,
                contentDescription = "Settings",
                tint = MaterialTheme.colors.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colors.secondary)
        ) {
            DropdownMenuItem(
                onClick = {
                    onQrCodeClick()
                    expanded = false
                }
            ) {
                Icon(
                    imageVector = AppIcons.QrScan,
                    contentDescription = "Scan QR",
                    tint = MaterialTheme.colors.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "QR", style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
            DropdownMenuItem(
                onClick = {
                    onIntent(VpnScreenIntent.ImportConfigFromClipboard)
                    expanded = false
                }
            ) {
                Icon(
                    imageVector = AppIcons.Clipboard,
                    contentDescription = "Clipboard",
                    tint = MaterialTheme.colors.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Буфер обмена", style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}
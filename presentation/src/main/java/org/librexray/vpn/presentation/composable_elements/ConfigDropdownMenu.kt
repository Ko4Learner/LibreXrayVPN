package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.intent.VpnScreenIntent

@Composable
fun ConfigDropDownMenu(
    onIntent: (VpnScreenIntent) -> Unit,
    onQrCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = AppIcons.Add,
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
//                Icon(
//                    imageVector = AppIcons.QrScan,
//                    contentDescription = "Сканировать QR-код",
//                    tint = MaterialTheme.colors.onSurface
//                )
//                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Сканировать QR-код", style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
            DropdownMenuItem(
                onClick = {
                    onIntent(VpnScreenIntent.ImportConfigFromClipboard)
                    expanded = false
                }
            ) {
//                Icon(
//                    imageVector = AppIcons.Clipboard,
//                    contentDescription = "Добавить из буфера обмена",
//                    tint = MaterialTheme.colors.onSurface
//                )
//                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Добавить из буфера обмена", style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}
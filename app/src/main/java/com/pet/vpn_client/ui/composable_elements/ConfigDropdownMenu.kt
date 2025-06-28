package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.pet.vpn_client.R
import com.pet.vpn_client.presentation.intent.VpnScreenIntent

@Composable
fun ConfigDropDownMenu(onIntent: (VpnScreenIntent) -> Unit, onQrCodeClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Image(
            modifier = Modifier.clickable { expanded = true },
            painter = painterResource(id = R.drawable.ic_list),
            contentDescription = "Settings"
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(text = "QR") }, onClick = {
                onQrCodeClick()
                expanded = false
            })
            DropdownMenuItem(text = { Text(text = "Буфер обмена") }, onClick = {
                onIntent(VpnScreenIntent.ImportConfigFromClipboard)
                expanded = false
            })
            DropdownMenuItem(text = { Text(text = "Файл") }, onClick = { expanded = false })
        }
    }
}
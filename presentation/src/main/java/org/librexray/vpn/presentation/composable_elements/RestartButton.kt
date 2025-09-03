package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.librexray.vpn.presentation.intent.VpnScreenIntent

@Composable
fun RestartButton(onIntent: (VpnScreenIntent) -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color = MaterialTheme.colors.secondary, shape = CircleShape)
            .clickable {
                onIntent(VpnScreenIntent.RestartConnection)
            },
        contentAlignment = Alignment.Center

    ) {
        Text(
            text = "R",
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.h6
        )
    }
}
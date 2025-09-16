package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.intent.VpnScreenIntent

@Composable
fun ConnectionTestButton(
    modifier: Modifier = Modifier,
    onIntent: (VpnScreenIntent) -> Unit,
    delayMs: Long?
) {
    Card(
        modifier = modifier
            .padding(top = 8.dp, bottom = 16.dp)
            .fillMaxWidth()
            .height(48.dp),
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
    ) {
        Row(
            modifier = modifier
                .padding(start = 16.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(text = delayMs?.let { "Delay: $it ms" } ?: "Нажмите для тестирования",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface)

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colors.primary
            ) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = { onIntent(VpnScreenIntent.TestConnection) }
                ) {
                    Icon(
                        painter = AppIcons.LatencyTest.rememberPainter(),
                        contentDescription = "Test connection",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
}
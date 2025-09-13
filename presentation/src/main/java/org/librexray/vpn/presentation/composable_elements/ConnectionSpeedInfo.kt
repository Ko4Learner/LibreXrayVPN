package org.librexray.vpn.presentation.composable_elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.domain.models.ConnectionSpeed
import java.util.Locale

@Composable
fun ConnectionSpeedInfo(
    modifier: Modifier = Modifier,
    connectionSpeed: ConnectionSpeed?
) {
    Surface(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "${Constants.LABEL_PROXY} ${Constants.ARROW_UP} ${fmtBps(connectionSpeed?.proxyUplinkBps)} " +
                        "${Constants.ARROW_DOWN} ${fmtBps(connectionSpeed?.proxyDownlinkBps)}",
                style = MaterialTheme.typography.body1.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colors.onSurface
            )
            Text(
                "${Constants.LABEL_DIRECT} ${Constants.ARROW_UP} ${fmtBps(connectionSpeed?.directUplinkBps)} " +
                        "${Constants.ARROW_DOWN} ${fmtBps(connectionSpeed?.directDownlinkBps)}",
                style = MaterialTheme.typography.body1.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

/**
 * Formats a bit-per-second [bps] value into a readable string.
 * Values are converted into MB/s, KB/s, or B/s depending on size.
 * Uses [Locale.ENGLISH] to ensure a dot as the decimal separator.
 * Returns "—" if [bps] is null.
 * @param bps Speed in bytes per second.
 * @return Formatted speed string.
 */
private fun fmtBps(bps: Double?): String {
    bps ?: return "—"
    val kb = bps / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1 -> String.format(Locale.ENGLISH, "%.2f ${Constants.UNIT_MB}", mb)
        kb >= 1 -> String.format(Locale.ENGLISH, "%.0f ${Constants.UNIT_KB}", kb)
        else -> String.format(Locale.ENGLISH, "%.0f ${Constants.UNIT_B}", bps)
    }
}
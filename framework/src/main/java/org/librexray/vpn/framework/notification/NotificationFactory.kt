package org.librexray.vpn.framework.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.coreandroid.utils.LocaleHelper
import org.librexray.vpn.domain.interfaces.repository.SettingsRepository
import org.librexray.vpn.framework.services.VPNService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Factory for localized foreground notifications used by the VPN service.
 *
 * Responsibilities:
 * - Produces a preconfigured [NotificationCompat.Builder] for persistent VPN status.
 * - Applies the user's locale to notification content.
 * - Wires actions (open app / restart / stop VPN) via [PendingIntentProvider].
 */
class NotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pendingIntentProvider: PendingIntentProvider,
    private val settingsRepository: SettingsRepository
) {
    //TODO строки в ресурсы
    /**
     * Creates a localized, preconfigured notification builder for the VPN service.
     */
    fun createNotificationBuilder(title: String): NotificationCompat.Builder {
        val locale = settingsRepository.getLocale()
        val localizedContext = LocaleHelper.updateLocale(context, locale)
        val channelId = "LibreXrayVPN"
        val notificationManager =
            localizedContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            title,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(localizedContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(createMainActivityPendingIntent(localizedContext))
            .addAction(
                R.drawable.outline_3d_rotation_24,
                "Перезапустить",
                createRestartServicePendingIntent(localizedContext)
            )
            .addAction(
                R.drawable.baseline_stop_24,
                "Выключить",
                createStopServicePendingIntent(localizedContext)
            )
    }

    /**
     * Creates a [PendingIntent] to open the main activity from the notification.
     *
     * @param context The context to use for creating the intent.
     * @return PendingIntent for opening the main activity.
     */
    private fun createMainActivityPendingIntent(context: Context): PendingIntent {
        return pendingIntentProvider.createMainActivityPendingIntent(context)
    }

    /**
     * Creates a [PendingIntent] to stop the VPN service from the notification.
     *
     * @param context The context to use for creating the intent.
     * @return PendingIntent for stopping the service.
     */
    private fun createStopServicePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, VPNService::class.java).apply {
            putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_STOP_SERVICE)
        }
        return PendingIntent.getService(
            context,
            REQUEST_CODE_STOP_SERVICE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a [PendingIntent] to restart the VPN service from the notification.
     *
     * @param context The context to use for creating the intent.
     * @return PendingIntent for restarting the service.
     */
    private fun createRestartServicePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, VPNService::class.java).apply {
            putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_RESTART_SERVICE)
        }
        return PendingIntent.getService(
            context,
            REQUEST_CODE_RESTART_SERVICE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    companion object {
        private const val REQUEST_CODE_STOP_SERVICE = 1
        private const val REQUEST_CODE_RESTART_SERVICE = 2
    }
}
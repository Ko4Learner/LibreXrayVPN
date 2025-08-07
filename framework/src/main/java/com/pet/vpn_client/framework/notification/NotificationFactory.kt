package com.pet.vpn_client.framework.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pet.vpn_client.core.R
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.LocaleHelper
import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
import com.pet.vpn_client.framework.services.VPNService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Factory class for creating VPN service notifications.
 *
 * Responsible for building localized, persistent notifications for the VPN service,
 * including actions for restarting and stopping the VPN.
 */
class NotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pendingIntentProvider: PendingIntentProvider,
    private val settingsRepository: SettingsRepository
) {
    //TODO строки в ресурсы
    /**
     * Creates and returns a localized persistent notification for the VPN service.
     *
     * @param title The title of the notification.
     * @return Configured [Notification] object.
     */
    fun createNotification(title: String): Notification {
        val locale = settingsRepository.getLocale()
        val localizedContext = LocaleHelper.updateLocale(context, locale)
        val channelId = "Vpn_Client"
        val notificationManager =
            localizedContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            title,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(localizedContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText("$title запущен")
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
            .setOngoing(true)
            .build()
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
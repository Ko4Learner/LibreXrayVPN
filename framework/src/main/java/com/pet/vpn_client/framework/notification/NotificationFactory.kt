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
import com.pet.vpn_client.core.utils.LocaleHelper
import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.framework.services.VPNService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    val pendingIntentProvider: PendingIntentProvider,
    val settingsManager: SettingsManager
) {
    fun createNotification(title: String): Notification {
        val locale = settingsManager.getLocale()
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

    private fun createMainActivityPendingIntent(context: Context): PendingIntent {
        return pendingIntentProvider.createMainActivityPendingIntent(context)
    }

    private fun createStopServicePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, VPNService::class.java).apply {
            putExtra("COMMAND", "STOP_SERVICE")
        }
        return PendingIntent.getService(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createRestartServicePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, VPNService::class.java).apply {
            putExtra("COMMAND", "RESTART_SERVICE")
        }
        return PendingIntent.getService(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
package com.pet.vpn_client.framework.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pet.vpn_client.R
import com.pet.vpn_client.framework.services.ProxyService
import com.pet.vpn_client.framework.services.VPNService
import com.pet.vpn_client.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationFactory @Inject constructor(@ApplicationContext private val context: Context) {

    fun createNotification(title: String): Notification {
        val channelId = "Vpn_Client"
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            title,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText("$title запущен")
            .setContentIntent(createMainActivityPendingIntent())
            .addAction(
                R.drawable.outline_3d_rotation_24,
                "Перезапустить",
                createRestartServicePendingIntent(title)
            )
            .addAction(
                R.drawable.baseline_stop_24,
                "Выключить",
                createStopServicePendingIntent(title)
            )
            .setOngoing(true)
            .build()
    }

    private fun createMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopServicePendingIntent(title: String): PendingIntent {
        val intent = when (title) {
            "Proxy" -> {
                Intent(context, ProxyService::class.java).apply {
                    putExtra("COMMAND", "STOP_SERVICE")
                }
            }

            else -> {
                Intent(context, VPNService::class.java).apply {
                    putExtra("COMMAND", "STOP_SERVICE")
                }
            }
        }
        return PendingIntent.getService(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createRestartServicePendingIntent(title: String): PendingIntent {
        val intent = when (title) {
            "Proxy" -> {
                Intent(context, ProxyService::class.java).apply {
                    putExtra("COMMAND", "RESTART_SERVICE")
                }
            }

            else -> {
                Intent(context, VPNService::class.java).apply {
                    putExtra("COMMAND", "RESTART_SERVICE")
                }
            }
        }
        return PendingIntent.getService(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
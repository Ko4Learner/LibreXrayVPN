package com.pet.vpn_client.framework.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.ServiceControl
import com.pet.vpn_client.domain.interfaces.ServiceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProxyService : Service(), ServiceControl {
    @Inject
    lateinit var serviceManager: ServiceManager
    override fun onCreate() {
        super.onCreate()
        serviceManager.setService(this)
        showNotification()
    }

    private fun showNotification() {
        val channelId = "proxy_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "PROXY",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PROXY")
            .setContentText("PROXY сервис запущен")
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("COMMAND")) {
            "START_PROXY" -> {
                Log.d(Constants.TAG, "START_PROXY")
                serviceManager.startCoreLoop()
            }

            "STOP_PROXY" -> {
                Log.d(Constants.TAG, "STOP_PROXY")
                onDestroy()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceManager.stopService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun getService(): Service {
        return this
    }

    override fun startService() {
        //empty
    }

    override fun stopService() {
        stopSelf()
    }

    override fun vpnProtect(socket: Int): Boolean {
        return true
    }

    //    //применение локали (язык) приложения
//    override fun attachBaseContext(newBase: Context?) {
//        val context = newBase?.let {
//            MyContextWrapper.wrap(newBase, settingsManager.getLocale())
//        }
//        super.attachBaseContext(context)
//    }
}
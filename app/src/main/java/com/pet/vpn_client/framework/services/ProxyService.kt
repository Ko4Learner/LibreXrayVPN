package com.pet.vpn_client.framework.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pet.vpn_client.domain.interfaces.ServiceControl
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.state.ServiceState
import com.pet.vpn_client.framework.notification.NotificationFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProxyService : Service(), ServiceControl {
    @Inject
    lateinit var serviceManager: ServiceManager
    @Inject
    lateinit var notificationFactory: NotificationFactory
    @Inject
    lateinit var serviceStateRepository: ServiceStateRepository

    override fun onCreate() {
        super.onCreate()
        startForeground(2, notificationFactory.createNotification("Proxy"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("COMMAND")) {
            "START_SERVICE" -> {
                serviceManager.startCoreLoop()
                serviceStateRepository.updateState(ServiceState.Connected)
            }

            "STOP_SERVICE" -> {
                serviceManager.stopCoreLoop()
                stopSelf()
                serviceStateRepository.updateState(ServiceState.Stopped)
                return START_NOT_STICKY
            }

            "RESTART_SERVICE" -> {
                serviceManager.restartService()
            }
        }
        return START_STICKY
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
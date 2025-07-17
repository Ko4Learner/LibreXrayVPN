package com.pet.vpn_client.framework.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pet.vpn_client.domain.interfaces.ServiceControl
import com.pet.vpn_client.domain.interfaces.ServiceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProxyService @Inject constructor(
    val serviceManager: ServiceManager
) : Service(), ServiceControl {

    override fun onCreate() {
        super.onCreate()
        serviceManager.setService(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceManager.startCoreLoop()
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
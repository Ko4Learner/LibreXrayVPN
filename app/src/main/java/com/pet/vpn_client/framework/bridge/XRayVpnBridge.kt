package com.pet.vpn_client.framework.bridge

import android.app.Service
import android.util.Log
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.data.mmkv.MMKVConfig
import com.pet.vpn_client.framework.services.ServiceControl
import com.pet.vpn_client.utils.IntentUtil
import com.pet.vpn_client.utils.Utils
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import java.lang.ref.SoftReference

class XRayVpnBridge {

    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())

    var serviceControl: SoftReference<ServiceControl>? = null
        set(value) {
            field = value
            Seq.setContext(value?.get()?.getService()?.applicationContext)
            Libv2ray.initCoreEnv(
                Utils.userAssetPath(value?.get()?.getService()),
                Utils.getDeviceIdForXUDPBaseKey()
            )
        }

//    fun startCoreLoop(): Boolean {
//        if (coreController.isRunning) {
//            return false
//        }
//
//        val service = getService() ?: return false
//        val guid = MMKVConfig.getSelectServer() ?: return false
//        val config = MmkvManager.decodeServerConfig(guid) ?: return false
//        val result = V2rayConfigManager.getV2rayConfig(service, guid)
//        if (!result.status)
//            return false
//
//        // Регистрирует BroadcastReceiver для получения уведомлений о событиях системы
//        try {
//            val mFilter = IntentFilter(AppConfig.BROADCAST_ACTION_SERVICE)
//            mFilter.addAction(Intent.ACTION_SCREEN_ON)
//            mFilter.addAction(Intent.ACTION_SCREEN_OFF)
//            mFilter.addAction(Intent.ACTION_USER_PRESENT)
//            ContextCompat.registerReceiver(service, mMsgReceive, mFilter, Utils.receiverFlags())
//        } catch (e: Exception) {
//            Log.e(AppConfig.TAG, "Failed to register broadcast receiver", e)
//            return false
//        }
//
//        currentConfig = config
//
//        // видимо в coreController.startLoop также coreController.isRunning становится true
//        try {
//            coreController.startLoop(result.content)
//        } catch (e: Exception) {
//            Log.e(AppConfig.TAG, "Failed to start Core loop", e)
//            return false
//        }
//
//        //если ядро не запустилось отправляет сообщение в UI и отключает уведомления
//        if (coreController.isRunning == false) {
//            MessageUtil.sendMsg2UI(service, Constants.MSG_STATE_START_FAILURE, "")
//            NotificationService.cancelNotification()
//            return false
//        }
//
//        try {
//            MessageUtil.sendMsg2UI(service, AppConfig.MSG_STATE_START_SUCCESS, "")
//            NotificationService.startSpeedNotification(currentConfig)
//            NotificationService.showNotification(currentConfig)
//            //запуск плагина связанного с HYSTERIA2
//            PluginUtil.runPlugin(service, config, result.socksPort)
//        } catch (e: Exception) {
//            Log.e(AppConfig.TAG, "Failed to startup service", e)
//            return false
//        }
//        return true
//    }
//
//    fun stopCoreLoop(): Boolean {
//        val service = getService() ?: return false
//
//        if (coreController.isRunning) {
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    coreController.stopLoop()
//                } catch (e: Exception) {
//                    Log.e(AppConfig.TAG, "Failed to stop V2Ray loop", e)
//                }
//            }
//        }
//
//        MessageUtil.sendMsg2UI(service, AppConfig.MSG_STATE_STOP_SUCCESS, "")
//        NotificationService.cancelNotification()
//
//        try {
//            service.unregisterReceiver(mMsgReceive)
//        } catch (e: Exception) {
//            Log.e(AppConfig.TAG, "Failed to unregister broadcast receiver", e)
//        }
//        PluginUtil.stopPlugin()
//
//        return true
//    }

    private fun getService(): Service? {
        return serviceControl?.get()?.getService()
    }

    fun queryStats(tag: String, link: String): Long {
        return coreController.queryStats(tag, link)
    }

    private fun measureV2rayDelay() {
        if (coreController.isRunning == false) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            //return@launch завершает корутину
            val service = getService() ?: return@launch
            var time = -1L
            var errorStr = ""

            try {
                //time = coreController.measureDelay(SettingsManager.getDelayTestUrl())
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to measure delay with primary URL", e)
                errorStr = e.message?.substringAfter("\":") ?: "empty message"
            }
            //если тест задержки не отработал по стандартному адресу, то проверяется по прописанному
            if (time == -1L) {
                try {
                    //time = coreController.measureDelay(SettingsManager.getDelayTestUrl(true))
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Failed to measure delay with alternative URL", e)
                    errorStr = e.message?.substringAfter("\":") ?: "empty message"
                }
            }

            val result = if (time >= 0) {
               // service.getString(R.string.connection_test_available, time)
                ""
            } else {
                //service.getString(R.string.connection_test_error, errorStr)
                ""
            }
            IntentUtil.sendMsg2UI(service, Constants.MSG_MEASURE_DELAY_SUCCESS, result)

            // Only fetch IP info if the delay test was successful
            if (time >= 0) {
                // показывает после успешной проверки в том числе и ip удаленного сервера
//                SpeedtestManager.getRemoteIPInfo()?.let { ip ->
//                    IntentUtil.sendMsg2UI(service, Constants.MSG_MEASURE_DELAY_SUCCESS, "$result\n$ip")
//                }
            }
        }
    }

    private inner class CoreCallback : CoreCallbackHandler {

        override fun startup(): Long {
            return 0
        }

        override fun shutdown(): Long {
            val serviceControl = serviceControl?.get() ?: return -1
            return try {
                serviceControl.stopService()
                0
            } catch (e: Exception) {
                -1
            }
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            return 0
        }
    }
}
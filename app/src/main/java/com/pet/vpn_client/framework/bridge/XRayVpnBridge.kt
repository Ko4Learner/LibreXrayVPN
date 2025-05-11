package com.pet.vpn_client.framework.bridge

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.utils.IntentUtil
import com.pet.vpn_client.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject

class XRayVpnBridge @Inject constructor(
    val storage: KeyValueStorage,
    val serviceManager: ServiceManager
) : CoreVpnBridge {

    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())
    private var currentConfig: ConfigProfileItem? = null

    override fun isRunning(): Boolean = coreController.isRunning

    override fun startCoreLoop(): Boolean {
        if (coreController.isRunning) {
            return false
        }

        val service = serviceManager.getService() ?: return false
        val guid = storage.getSelectServer() ?: return false
        val config = storage.decodeServerConfig(guid) ?: return false
//        val result = V2rayConfigManager.getV2rayConfig(service, guid)
//        if (!result.status)
//            return false

        // Регистрирует BroadcastReceiver для получения уведомлений о событиях системы
        try {
            val mFilter = IntentFilter(Constants.BROADCAST_ACTION_SERVICE)
            mFilter.addAction(Intent.ACTION_SCREEN_ON)
            mFilter.addAction(Intent.ACTION_SCREEN_OFF)
            mFilter.addAction(Intent.ACTION_USER_PRESENT)
            ContextCompat.registerReceiver(service as Context, serviceManager.getMsgReceive(), mFilter, Utils.receiverFlags())
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to register broadcast receiver", e)
            return false
        }

        currentConfig = config

        try {
            //coreController.startLoop(result.content)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to start Core loop", e)
            return false
        }

        if (coreController.isRunning == false) {
            IntentUtil.sendMsg2UI(service, Constants.MSG_STATE_START_FAILURE, "")
            //NotificationService.cancelNotification()
            return false
        }

        try {
            IntentUtil.sendMsg2UI(service, Constants.MSG_STATE_START_SUCCESS, "")
//            NotificationService.startSpeedNotification(currentConfig)
//            NotificationService.showNotification(currentConfig)
            //запуск плагина связанного с HYSTERIA2
//            PluginUtil.runPlugin(service, config, result.socksPort)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to startup service", e)
            return false
        }
        return true
    }

    override fun stopCoreLoop(): Boolean {
        val service = serviceManager.getService() ?: return false

        if (coreController.isRunning) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    coreController.stopLoop()
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Failed to stop V2Ray loop", e)
                }
            }
        }

        IntentUtil.sendMsg2UI(service as Context, Constants.MSG_STATE_STOP_SUCCESS, "")
        //NotificationService.cancelNotification()

        try {
            service.unregisterReceiver(serviceManager.getMsgReceive())
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to unregister broadcast receiver", e)
        }
        //PluginUtil.stopPlugin()

        return true
    }

    override fun queryStats(tag: String, link: String): Long {
        return coreController.queryStats(tag, link)
    }

    override fun measureV2rayDelay() {
        if (coreController.isRunning == false) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            //return@launch завершает корутину
            val service = serviceManager.getService() ?: return@launch
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
            IntentUtil.sendMsg2UI(service as Context, Constants.MSG_MEASURE_DELAY_SUCCESS, result)

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
            val serviceControl = serviceManager.getService() ?: return -1
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
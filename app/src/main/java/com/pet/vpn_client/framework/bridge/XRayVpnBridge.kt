package com.pet.vpn_client.framework.bridge

import android.content.Context
import android.util.Log
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.data.ConfigManager
import com.pet.vpn_client.data.SettingsManager
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject

class XRayVpnBridge @Inject constructor(
    val context: Context,
    val storage: KeyValueStorage,
    val serviceManager: ServiceManager,
    val configManager: ConfigManager,
    val settingsManager: SettingsManager
) : CoreVpnBridge {

    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())
//    private var currentConfig: ConfigProfileItem? = null

    override fun isRunning(): Boolean = coreController.isRunning

    override fun startCoreLoop(): Boolean {
        if (coreController.isRunning) {
            return false
        }
        val guid = storage.getSelectServer() ?: return false
//        val config = storage.decodeServerConfig(guid) ?: return false
        val result = configManager.getV2rayConfig(context, guid)
        if (!result.status) return false

        if (!serviceManager.registerReceiver()) return false

//        currentConfig = config

        try {
            coreController.startLoop(result.content)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to start Core loop", e)
            return false
        }
        return true
    }

    override fun stopCoreLoop(): Boolean {
        if (coreController.isRunning) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    coreController.stopLoop()
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Failed to stop V2Ray loop", e)
                }
            }
        }
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
            serviceManager.getService() ?: return@launch
            var time = -1L
            var errorStr = ""

            try {
                time = coreController.measureDelay(settingsManager.getDelayTestUrl())
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to measure delay with primary URL", e)
                errorStr = e.message?.substringAfter("\":") ?: "empty message"
            }

            if (time == -1L) {
                try {
                    time = coreController.measureDelay(settingsManager.getDelayTestUrl(true))
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Failed to measure delay with alternative URL", e)
                    errorStr = e.message?.substringAfter("\":") ?: "empty message"
                }
            }
            serviceManager.measureDelay(time)
        }
    }

    private inner class CoreCallback : CoreCallbackHandler {

        override fun startup(): Long {
            return 0
        }

        override fun shutdown(): Long {
            serviceManager.getService() ?: return -1
            return try {
                serviceManager.stopService()
                0
            } catch (_: Exception) {
                -1
            }
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            return 0
        }
    }
}
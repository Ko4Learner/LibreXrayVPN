package com.pet.vpn_client.framework.bridge

import android.content.Context
import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.SettingsManager
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
    override fun isRunning(): Boolean = coreController.isRunning

    override fun startCoreLoop(): Boolean {
        if (coreController.isRunning) return false
        val guid = storage.getSelectServer() ?: return false
        val result = configManager.getCoreConfig(guid)
        if (!result.status) return false

        try {
            coreController.startLoop(result.content)
            coreController.isRunning = true
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to start Core loop", e)
            return false
        }
        return true
    }

    override fun stopCoreLoop() {
        if (coreController.isRunning) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    coreController.stopLoop()
                } catch (e: Exception) {
                    Log.e(Constants.TAG, "Failed to stop V2Ray loop", e)
                }
            }
        }
    }

    //TODO необходимость?
    override fun queryStats(tag: String, link: String): Long {
        return coreController.queryStats(tag, link)
    }

    override suspend fun measureDelay(): Long? {
        if (!coreController.isRunning) return null
        var time = -1L

        try {
            time = coreController.measureDelay(settingsManager.getDelayTestUrl())
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to measure delay with primary URL", e)
        }

        if (time == -1L) {
            try {
                time = coreController.measureDelay(settingsManager.getDelayTestUrl(true))
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to measure delay with alternative URL", e)
            }
        }
        return time
    }

    private inner class CoreCallback : CoreCallbackHandler {
        override fun startup(): Long {
            return 0
        }

        override fun shutdown(): Long {
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
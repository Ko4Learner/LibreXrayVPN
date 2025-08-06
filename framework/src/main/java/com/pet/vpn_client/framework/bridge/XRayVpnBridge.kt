package com.pet.vpn_client.framework.bridge

import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject

class XRayVpnBridge @Inject constructor(
    private val storage: KeyValueStorage,
    private val serviceManager: ServiceManager,
    private val configRepository: ConfigRepository
) : CoreVpnBridge {
    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())
    override fun isRunning(): Boolean = coreController.isRunning

    override fun startCoreLoop(): Boolean {
        if (coreController.isRunning) return false
        val guid = storage.getSelectServer() ?: return false
        val result = configRepository.getCoreConfig(guid)
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
                    Log.e(Constants.TAG, "Failed to stop XRay loop", e)
                }
            }
        }
    }

    //TODO получение статистики по тегу, использовать в будущем на главном экране
    override fun queryStats(tag: String, link: String): Long {
        return coreController.queryStats(tag, link)
    }

    override suspend fun measureDelay(): Long? {
        if (!coreController.isRunning) return null
        var time = -1L

        try {
            time = coreController.measureDelay(Constants.DELAY_TEST_URL)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to measure delay with primary URL", e)
        }

        if (time == -1L) {
            try {
                time = coreController.measureDelay(Constants.DELAY_TEST_URL2)
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
package com.pet.vpn_client.framework.bridge

import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.CoreConfigProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject

/**
 * Bridge for interacting with the Xray VPN core.
 * Manages the lifecycle of the Xray engine, providing methods to start, stop, and query the core.
 * Handles configuration and communication with lower-level VPN engine logic.
 */
class XRayVpnBridge @Inject constructor(
    private val storage: KeyValueStorage,
    private val serviceManager: ServiceManager,
    private val coreConfigProvider: CoreConfigProvider
) : CoreVpnBridge {
    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())
    override fun isRunning(): Boolean = coreController.isRunning

    /**
     * Starts the Xray core loop if not already running.
     *
     * @return true if started successfully, false if already running or on failure.
     */
    override fun startCoreLoop(): Boolean {
        if (coreController.isRunning) return false
        val guid = storage.getSelectServer() ?: return false
        val result = coreConfigProvider.getCoreConfig(guid)
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

    //TODO разобраться с корутиной
    /**
     * Stops the Xray core loop asynchronously if running.
     * Launches coroutine for IO operations.
     */
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
    /**
     * Queries traffic statistics by tag and link.
     *
     * @param tag The statistics tag.
     * @param link The link identifier.
     * @return The queried statistics value.
     */
    override fun queryStats(tag: String, link: String): Long {
        return coreController.queryStats(tag, link)
    }

    /**
     * Measures network delay using the Xray core.
     * Attempts primary and fallback test URLs.
     *
     * @return Measured delay in milliseconds, or null if core is not running.
     */
    override suspend fun measureDelay(): Long? {
        if (!coreController.isRunning) return null
        var time = -1L

        try {
            time = coreController.measureDelay(DELAY_TEST_URL)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to measure delay with primary URL", e)
        }

        if (time == -1L) {
            try {
                time = coreController.measureDelay(DELAY_TEST_URL2)
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to measure delay with alternative URL", e)
            }
        }
        return time
    }

    /**
     * Callback handler for core lifecycle events.
     */
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

    companion object{
        private const val DELAY_TEST_URL = "https://www.gstatic.com/generate_204"
        private const val DELAY_TEST_URL2 = "https://www.google.com/generate_204"
    }
}
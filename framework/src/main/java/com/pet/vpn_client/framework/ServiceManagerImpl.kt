package com.pet.vpn_client.framework

import android.content.Context
import android.content.Intent
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.state.ServiceState
import com.pet.vpn_client.framework.services.VPNService
import com.pet.vpn_client.core.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

/**
 * Implements management logic for VPN service operations.
 *
 * Responsible for starting, stopping, and restarting the VPN service and core loop.
 * Handles configuration validation, provides connection state, and interacts with
 * persistent storage and the underlying VPN core bridge.
 */
class ServiceManagerImpl @Inject constructor(
    private val storage: KeyValueStorage,
    private val coreVpnBridgeProvider: Provider<CoreVpnBridge>,
    private val stateRepository: ServiceStateRepository,
    @ApplicationContext private val context: Context
) : ServiceManager {
    private val coreVpnBridge: CoreVpnBridge by lazy { coreVpnBridgeProvider.get() }
    private var currentConfig: ConfigProfileItem? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Starts the VPN service if a server is selected.
     */
    override fun startService() {
        if (storage.getSelectServer().isNullOrEmpty()) {
            return
        }
        startContextService()
    }

    /**
     * Restarts the VPN service, waiting for the service state to become idle or stopped.
     */
    override fun restartService() {
        stopService()
        scope.launch {
            stateRepository.serviceState
                .filter { it is ServiceState.Idle || it is ServiceState.Stopped }
                .first()
            startContextService()
        }
    }

    /**
     * Starts the VPN service as a foreground service with validated configuration.
     * Only starts if not already running and config is valid.
     */
    private fun startContextService() {
        if (coreVpnBridge.isRunning()) return
        val guid = storage.getSelectServer() ?: return
        val config = storage.decodeServerConfig(guid) ?: return
        if (!Utils.isValidUrl(config.server)
            && !Utils.isIpAddress(config.server)
        ) return

        val intent = Intent(context, VPNService::class.java).apply {
            putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_START_SERVICE)
        }
        context.startForegroundService(intent)
    }

    /**
     * Stops the VPN service.
     */
    override fun stopService() {
        val intent = Intent(context, VPNService::class.java).apply {
            putExtra(Constants.EXTRA_COMMAND, Constants.COMMAND_STOP_SERVICE)
        }
        context.startForegroundService(intent)
    }

    /**
     * Returns the name of the currently running server.
     */
    override fun getRunningServerName() = currentConfig?.remarks.orEmpty()

    /**
     * Starts the core VPN loop.
     * @return true if started and running, false otherwise.
     */
    override fun startCoreLoop(): Boolean {
        return if (coreVpnBridge.startCoreLoop()) {
            coreVpnBridge.isRunning()
        } else false
    }

    /**
     * Stops the core VPN loop.
     */
    override fun stopCoreLoop() {
        coreVpnBridge.stopCoreLoop()
    }

    /**
     * Measures and returns the current network delay via the VPN core.
     */
    override suspend fun measureDelay(): Long? {
        return coreVpnBridge.measureDelay()
    }
}
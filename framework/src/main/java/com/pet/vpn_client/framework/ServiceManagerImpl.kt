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
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implements management logic for VPN service operations.
 *
 * Responsible for starting, stopping, and restarting the VPN service and core loop.
 * Handles configuration validation, provides connection state, and interacts with
 * persistent storage and the underlying VPN core bridge.
 */
class ServiceManagerImpl @Inject constructor(
    private val storage: KeyValueStorage,
    private val coreVpnBridgeLazy: Lazy<CoreVpnBridge>,
    private val stateRepository: ServiceStateRepository,
    @ApplicationContext private val context: Context
) : ServiceManager {
    private val coreVpnBridge: CoreVpnBridge get() = coreVpnBridgeLazy.get()
    private var currentConfig: ConfigProfileItem? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Starts the VPN service if a server is selected.
     */
    override fun startService() {
        if (storage.getSelectedServer().isNullOrEmpty()) {
            return
        }
        startContextService()
    }

    /**
     * Restarts the VPN service, awaiting both service and core to be fully stopped.
     */
    override fun restartService() {
        stopService()
        scope.launch {
            combine(
                stateRepository.serviceState,
                coreVpnBridge.coreState
            ) { service, coreRunning ->
                (service is ServiceState.Idle || service is ServiceState.Stopped) && !coreRunning
            }
                .first { it }
            startContextService()
        }
    }

    /**
     * Starts the VPN service as a foreground service with validated configuration.
     * Only starts if not already running and config is valid.
     */
    private fun startContextService() {
        if (coreVpnBridge.coreState.value) return
        val guid = storage.getSelectedServer() ?: return
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
            coreVpnBridge.coreState.value
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
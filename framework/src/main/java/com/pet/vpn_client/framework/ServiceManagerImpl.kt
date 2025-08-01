package com.pet.vpn_client.framework

import android.content.Context
import android.content.Intent
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.state.ServiceState
import com.pet.vpn_client.framework.services.VPNService
import com.pet.vpn_client.core.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class ServiceManagerImpl @Inject constructor(
    private val storage: KeyValueStorage,
    private val coreVpnBridgeProvider: Provider<CoreVpnBridge>,
    private val stateRepository: ServiceStateRepository,
    private val context: Context
) : ServiceManager {

    private val coreVpnBridge: CoreVpnBridge by lazy { coreVpnBridgeProvider.get() }
    private var currentConfig: ConfigProfileItem? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun startService(): Boolean {
        if (storage.getSelectServer().isNullOrEmpty()) {
            return false
        }
        startContextService()
        return true
    }

    override fun restartService() {
        stopService()
        scope.launch {
            stateRepository.serviceState
                .filter { it is ServiceState.Idle || it is ServiceState.Stopped }
                .first()
            startContextService()
        }
    }

    private fun startContextService() {
        if (coreVpnBridge.isRunning()) return
        val guid = storage.getSelectServer() ?: return
        val config = storage.decodeServerConfig(guid) ?: return
        if (!Utils.isValidUrl(config.server)
            && !Utils.isIpAddress(config.server)
        ) return

        val intent = Intent(context, VPNService::class.java).apply {
            putExtra("COMMAND", "START_SERVICE")
        }
        context.startForegroundService(intent)
    }

    override fun stopService() {
        val intent = Intent(context, VPNService::class.java).apply {
            putExtra("COMMAND", "STOP_SERVICE")
        }
        context.startForegroundService(intent)
    }

    override fun getRunningServerName() = currentConfig?.remarks.orEmpty()

    override fun startCoreLoop(): Boolean {
        return if (coreVpnBridge.startCoreLoop()) {
            coreVpnBridge.isRunning()
        } else false
    }

    override fun stopCoreLoop() {
        coreVpnBridge.stopCoreLoop()
    }

    override suspend fun measureDelay(): Long? {
        return coreVpnBridge.measureDelay()
    }
}
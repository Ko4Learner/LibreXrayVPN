package com.pet.vpn_client.framework

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.state.ServiceState
import com.pet.vpn_client.framework.services.ProxyService
import com.pet.vpn_client.framework.services.VPNService
import com.pet.vpn_client.utils.Utils
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

    override fun startServiceFromToggle(): Boolean {
        if (storage.getSelectServer().isNullOrEmpty()) {
            return false
        }
        startContextService()
        return true
    }

    override fun startService(guid: String?) {
        if (guid != null) {
            storage.setSelectServer(guid)
        }
        startContextService()
    }

    override fun stopService() {
        val intent = if ((storage.decodeSettingsString(Constants.PREF_MODE)
                ?: Constants.VPN) == Constants.VPN
        ) {
            Intent(context, VPNService::class.java).apply {
                putExtra("COMMAND", "STOP_SERVICE")
            }
        } else {
            Intent(context, ProxyService::class.java).apply {
                putExtra("COMMAND", "STOP_SERVICE")
            }
        }
        context.startForegroundService(intent)
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

    override fun getRunningServerName() = currentConfig?.remarks.orEmpty()

    override fun startCoreLoop(): Boolean {
        if (coreVpnBridge.startCoreLoop()) {
            if (!coreVpnBridge.isRunning()) {
                return false
            }
            try {
//            NotificationService.startSpeedNotification(currentConfig)
//            NotificationService.showNotification(currentConfig)
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to startup service", e)
                return false
            }
            return true
        } else return false
    }

    override fun stopCoreLoop() {
        coreVpnBridge.stopCoreLoop()
    }

    override suspend fun measureDelay(): Long? {
        return coreVpnBridge.measureDelay()
    }

    private fun startContextService() {
        if (coreVpnBridge.isRunning()) {
            Log.d(Constants.TAG, "Service is already running")
            return
        }
        val guid = storage.getSelectServer() ?: return
        val config = storage.decodeServerConfig(guid) ?: return
        if (!Utils.isValidUrl(config.server)
            && !Utils.isIpAddress(config.server)
        ) return

        val intent = if ((storage.decodeSettingsString(Constants.PREF_MODE)
                ?: Constants.VPN) == Constants.VPN
        ) {
            Intent(context, VPNService::class.java).apply {
                putExtra("COMMAND", "START_SERVICE")
            }
        } else {
            Intent(context, ProxyService::class.java).apply {
                putExtra("COMMAND", "START_SERVICE")
            }
        }
        context.startForegroundService(intent)
    }
}
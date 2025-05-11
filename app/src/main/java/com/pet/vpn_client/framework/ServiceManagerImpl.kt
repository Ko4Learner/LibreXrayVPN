package com.pet.vpn_client.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceControl
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.framework.services.VPNService
import com.pet.vpn_client.utils.IntentUtil
import com.pet.vpn_client.utils.Utils
import java.lang.ref.SoftReference
import javax.inject.Inject

class ServiceManagerImpl @Inject constructor(
    val storage: KeyValueStorage,
    val coreVpnBridge: CoreVpnBridge
) : ServiceManager {

    var serviceControl: SoftReference<ServiceControl>? = null
    private val mMsgReceive = ReceiveMessageHandler()
    private var currentConfig: ConfigProfileItem? = null

    override fun setService(service: ServiceControl) {
        serviceControl = SoftReference(service)
    }

    override fun getService(): ServiceControl? {
        return serviceControl?.get()?.getService() as ServiceControl?
    }

    override fun getMsgReceive(): BroadcastReceiver {
        return mMsgReceive
    }

    override fun startServiceFromToggle(context: Context): Boolean {
        if (storage.getSelectServer().isNullOrEmpty()) {
            //context.toast(R.string.app_tile_first_use)
            return false
        }
        startContextService(context)
        return true
    }

    override fun startService(context: Context, guid: String?) {
        if (guid != null) {
            storage.setSelectServer(guid)
        }
        startContextService(context)
    }

    override fun stopService(context: Context) {
        //context.toast(R.string.toast_services_stop)
        IntentUtil.sendMsg2Service(context, Constants.MSG_STATE_STOP, "")
    }

    override fun getRunningServerName() = currentConfig?.remarks.orEmpty()
    override fun startCoreLoop(): Boolean {
        return coreVpnBridge.startCoreLoop()
    }

    override fun stopCoreLoop(): Boolean {
        return coreVpnBridge.stopCoreLoop()
    }

    private fun startContextService(context: Context) {
        if (coreVpnBridge.isRunning()) {
            return
        }
        val guid = storage.getSelectServer() ?: return
        val config = storage.decodeServerConfig(guid) ?: return
        if (config.configType != EConfigType.CUSTOM
            && !Utils.isValidUrl(config.server)
            && !Utils.isIpAddress(config.server)
        ) return

        //определяет возможность использования впн туннеля из Lan сети другими устройствами
        if (storage.decodeSettingsBool(Constants.PREF_PROXY_SHARING) == true) {
            //context.toast(R.string.toast_warning_pref_proxysharing_short)
        } else {
            //Оставить только это
            //context.toast(R.string.toast_services_start)
        }

        val intent = if ((storage.decodeSettingsString(Constants.PREF_MODE)
                ?: Constants.VPN) == Constants.VPN
        ) {
            Intent(context.applicationContext, VPNService::class.java)
        } else {
            //TODO заменить на Proxy
            Intent(context.applicationContext, VPNService::class.java)
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }


    private inner class ReceiveMessageHandler : BroadcastReceiver() {

        override fun onReceive(ctx: Context?, intent: Intent?) {
            val serviceControl = serviceControl?.get() ?: return

            when (intent?.getIntExtra("key", 0)) {
                Constants.MSG_REGISTER_CLIENT -> {
                    if (coreVpnBridge.isRunning()) {
                        IntentUtil.sendMsg2UI(
                            serviceControl.getService(),
                            Constants.MSG_STATE_RUNNING,
                            ""
                        )
                    } else {
                        IntentUtil.sendMsg2UI(
                            serviceControl.getService(),
                            Constants.MSG_STATE_NOT_RUNNING,
                            ""
                        )
                    }
                }

                Constants.MSG_UNREGISTER_CLIENT -> {
                    // nothing to do
                }

                Constants.MSG_STATE_START -> {
                    // nothing to do
                }

                Constants.MSG_STATE_STOP -> {
                    serviceControl.stopService()
                }

                Constants.MSG_STATE_RESTART -> {
                    serviceControl.stopService()
                    Thread.sleep(500L)
                    startService(serviceControl.getService())
                }

                Constants.MSG_MEASURE_DELAY -> {
                    coreVpnBridge.measureV2rayDelay()
                }
            }

//            when (intent?.action) {
//                Intent.ACTION_SCREEN_OFF -> {
//                    NotificationService.stopSpeedNotification(currentConfig)
//                }
//
//                Intent.ACTION_SCREEN_ON -> {
//                    NotificationService.startSpeedNotification(currentConfig)
//                }
//            }
        }
    }
}
package com.pet.vpn_client.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceControl
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.framework.services.ProxyService
import com.pet.vpn_client.framework.services.VPNService
import com.pet.vpn_client.utils.Utils
import java.lang.ref.SoftReference
import javax.inject.Inject
import javax.inject.Provider

class ServiceManagerImpl @Inject constructor(
    val storage: KeyValueStorage,
    val coreVpnBridgeProvider: Provider<CoreVpnBridge>,
    val context: Context
) : ServiceManager {

    private val coreVpnBridge: CoreVpnBridge by lazy { coreVpnBridgeProvider.get() }
    var serviceControl: SoftReference<ServiceControl>? = null
    private val mMsgReceive = ReceiveMessageHandler()
    private var currentConfig: ConfigProfileItem? = null

    override fun setService(service: ServiceControl) {
        serviceControl = SoftReference(service)
    }

    override fun getService(): ServiceControl? {
        return serviceControl?.get()?.getService() as ServiceControl?
    }

//    override fun getMsgReceive(): BroadcastReceiver {
//        return mMsgReceive
//    }

    override fun startServiceFromToggle(): Boolean {
        if (storage.getSelectServer().isNullOrEmpty()) {
            //context.toast(R.string.app_tile_first_use)
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
        //context.toast(R.string.toast_services_stop)
        val service = serviceControl?.get() ?: return
        service.stopService()
    }

    override fun getRunningServerName() = currentConfig?.remarks.orEmpty()

    override fun startCoreLoop(): Boolean {
        if (coreVpnBridge.startCoreLoop()) {
//            registerReceiver()
            if (coreVpnBridge.isRunning() == false) {
                //IntentUtil.sendMsg2UI(context, Constants.MSG_STATE_START_FAILURE, "")
                //NotificationService.cancelNotification()
                return false
            }

            try {
                //IntentUtil.sendMsg2UI(context, Constants.MSG_STATE_START_SUCCESS, "")
//            NotificationService.startSpeedNotification(currentConfig)
//            NotificationService.showNotification(currentConfig)
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to startup service", e)
                return false
            }
            return true
        } else return false
    }

    override fun stopCoreLoop(): Boolean {
        coreVpnBridge.stopCoreLoop()
        //IntentUtil.sendMsg2UI(context, Constants.MSG_STATE_STOP_SUCCESS, "")
        //NotificationService.cancelNotification()
        unregisterReceiver()
        return true
    }

    override fun measureDelay(time: Long) {
        val result = if (time >= 0) {
            // context.getString(R.string.connection_test_available, time)
            ""
        } else {
            //context.getString(R.string.connection_test_error, errorStr)
            ""
        }
        //IntentUtil.sendMsg2UI(context, Constants.MSG_MEASURE_DELAY_SUCCESS, result)
        if (time >= 0) {
            // показывает после успешной проверки в том числе и ip удаленного сервера
//                SpeedtestManager.getRemoteIPInfo()?.let { ip ->
//                    IntentUtil.sendMsg2UI(context, Constants.MSG_MEASURE_DELAY_SUCCESS, "$result\n$ip")
//                }
        }
    }

    override fun registerReceiver(): Boolean {
        val guid = storage.getSelectServer() ?: return false
        currentConfig = storage.decodeServerConfig(guid) ?: return false
        val service = serviceControl?.get() ?: return false
        try {
            val mFilter = IntentFilter(Constants.BROADCAST_ACTION_SERVICE)
            mFilter.addAction(Intent.ACTION_SCREEN_ON)
            mFilter.addAction(Intent.ACTION_SCREEN_OFF)
            mFilter.addAction(Intent.ACTION_USER_PRESENT)
            ContextCompat.registerReceiver(
                service as Context,
                mMsgReceive,
                mFilter,
                Utils.receiverFlags()
            )
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to register broadcast receiver", e)
            return false
        }
        return true
    }

    override fun unregisterReceiver() {
        val service = serviceControl?.get() ?: return
        try {
            (service as Context).unregisterReceiver(mMsgReceive)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to unregister broadcast receiver", e)
        }
    }

    private fun startContextService() {
        if (coreVpnBridge.isRunning()) {
            return
        }
        val guid = storage.getSelectServer() ?: return
        val config = storage.decodeServerConfig(guid) ?: return
        if (!Utils.isValidUrl(config.server)
            && !Utils.isIpAddress(config.server)
        ) return

        //определяет возможность использования впн туннеля из Lan сети другими устройствами
        if (storage.decodeSettingsBool(Constants.PREF_PROXY_SHARING) == true) {
            //context.toast(R.string.toast_warning_pref_proxysharing_short)
        } else {
            //!!!_Оставить только это
            //context.toast(R.string.toast_services_start)
        }

        val intent = if ((storage.decodeSettingsString(Constants.PREF_MODE)
                ?: Constants.VPN) == Constants.VPN
        ) {
            Intent(context, VPNService::class.java)
        } else {
            Intent(context, ProxyService::class.java)
        }

        context.startForegroundService(intent)
    }

    //TODO ПОЧЕМУ НЕТ СТАРТА СЕРВИСА?
    private inner class ReceiveMessageHandler : BroadcastReceiver() {

        override fun onReceive(ctx: Context?, intent: Intent?) {
            val serviceControl = serviceControl?.get() ?: return

            when (intent?.getIntExtra("key", 0)) {
                Constants.MSG_REGISTER_CLIENT -> {
                    if (coreVpnBridge.isRunning()) {
//                        IntentUtil.sendMsg2UI(
//                            serviceControl.getService(),
//                            Constants.MSG_STATE_RUNNING,
//                            ""
//                        )
                    } else {
//                        IntentUtil.sendMsg2UI(
//                            serviceControl.getService(),
//                            Constants.MSG_STATE_NOT_RUNNING,
//                            ""
//                        )
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
                    // startService(serviceControl.getService().)
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
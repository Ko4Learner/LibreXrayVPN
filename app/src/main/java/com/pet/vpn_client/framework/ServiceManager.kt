package com.pet.vpn_client.framework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.framework.services.ServiceControl
import com.pet.vpn_client.utils.IntentUtil
import com.pet.vpn_client.utils.Utils
import go.Seq
import libv2ray.Libv2ray
import java.lang.ref.SoftReference
import kotlin.text.get

class ServiceManager {

    var serviceControl: SoftReference<ServiceControl>? = null
        set(value) {
            field = value
            Seq.setContext(value?.get()?.getService()?.applicationContext)
            Libv2ray.initCoreEnv(
                Utils.userAssetPath(value?.get()?.getService()),
                Utils.getDeviceIdForXUDPBaseKey()
            )
        }

//    private inner class ReceiveMessageHandler : BroadcastReceiver() {
//
//        override fun onReceive(ctx: Context?, intent: Intent?) {
//            val serviceControl = serviceControl?.get() ?: return
//            when (intent?.getIntExtra("key", 0)) {
//                Constants.MSG_REGISTER_CLIENT -> {
//                    if (coreController.isRunning) {
//                        IntentUtil.sendMsg2UI(
//                            serviceControl.getService(),
//                            Constants.MSG_STATE_RUNNING,
//                            ""
//                        )
//                    } else {
//                        IntentUtil.sendMsg2UI(
//                            serviceControl.getService(),
//                            Constants.MSG_STATE_NOT_RUNNING,
//                            ""
//                        )
//                    }
//                }
//
//                Constants.MSG_UNREGISTER_CLIENT -> {
//                    // nothing to do
//                }
//
//                Constants.MSG_STATE_START -> {
//                    // nothing to do
//                }
//
//                Constants.MSG_STATE_STOP -> {
//                    serviceControl.stopService()
//                }
//
//                Constants.MSG_STATE_RESTART -> {
//                    serviceControl.stopService()
//                    Thread.sleep(500L)
////                    startVService(serviceControl.getService())
//                }
//
//                Constants.MSG_MEASURE_DELAY -> {
////                    measureV2rayDelay()
//                }
//            }
//
////            when (intent?.action) {
////                Intent.ACTION_SCREEN_OFF -> {
////                    NotificationService.stopSpeedNotification(currentConfig)
////                }
////
////                Intent.ACTION_SCREEN_ON -> {
////                    NotificationService.startSpeedNotification(currentConfig)
////                }
////            }
//        }
//    }
}
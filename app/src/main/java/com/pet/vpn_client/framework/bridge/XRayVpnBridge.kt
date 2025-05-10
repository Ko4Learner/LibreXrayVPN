package com.pet.vpn_client.framework.bridge

import com.pet.vpn_client.framework.services.ServiceControl
import com.pet.vpn_client.utils.Utils
import go.Seq
import java.lang.ref.SoftReference
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

class XRayVpnBridge {

    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())


    var serviceControl: SoftReference<ServiceControl>? = null
        set(value) {
            field = value
            Seq.setContext(value?.get()?.getService()?.applicationContext)
            Libv2ray.initCoreEnv(
                Utils.userAssetPath(value?.get()?.getService()),
                Utils.getDeviceIdForXUDPBaseKey()
            )
        }


    private inner class CoreCallback : CoreCallbackHandler {

        override fun startup(): Long {
            return 0
        }

        override fun shutdown(): Long {
            val serviceControl = serviceControl?.get() ?: return -1
            return try {
                serviceControl.stopService()
                0
            } catch (e: Exception) {
                -1
            }
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            return 0
        }
    }

//    private inner class ReceiveMessageHandler : BroadcastReceiver() {
//        /**
//         * Handles received broadcast messages.
//         * Processes service control messages and screen state changes.
//         * @param ctx The context in which the receiver is running.
//         * @param intent The intent being received.
//         */
//        override fun onReceive(ctx: Context?, intent: Intent?) {
//            val serviceControl = serviceControl?.get() ?: return
//            when (intent?.getIntExtra("key", 0)) {
//                Constants.MSG_REGISTER_CLIENT -> {
//                    if (coreController.isRunning) {
//                        MessageUtil.sendMsg2UI(serviceControl.getService(), AppConfig.MSG_STATE_RUNNING, "")
//                    } else {
//                        MessageUtil.sendMsg2UI(serviceControl.getService(), AppConfig.MSG_STATE_NOT_RUNNING, "")
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
//                    startVService(serviceControl.getService())
//                }
//
//                Constants.MSG_MEASURE_DELAY -> {
//                    measureV2rayDelay()
//                }
//            }
//
//            when (intent?.action) {
//                Intent.ACTION_SCREEN_OFF -> {
//                    NotificationService.stopSpeedNotification(currentConfig)
//                }
//
//                Intent.ACTION_SCREEN_ON -> {
//                    NotificationService.startSpeedNotification(currentConfig)
//                }
//            }
//        }
//    }
}
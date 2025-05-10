package com.pet.vpn_client.data.repository_impl

import com.pet.vpn_client.data.services.ServiceControl
import com.pet.vpn_client.domain.repository.VpnRepository
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import javax.inject.Inject
import libv2ray.Libv2ray
import java.lang.ref.SoftReference

class VpnRepositoryImpl @Inject constructor() : VpnRepository {

//    private val coreController: CoreController = Libv2ray.newCoreController(CoreCallback())
//    private val mMsgReceive = ReceiveMessageHandler()
//    private var currentConfig: ProfileItem? = null
//
//    var serviceControl: SoftReference<ServiceControl>? = null
//        set(value) {
//            field = value
//            Seq.setContext(value?.get()?.getService()?.applicationContext)
//            Libv2ray.initCoreEnv(
//                Utils.userAssetPath(value?.get()?.getService()),
//                Utils.getDeviceIdForXUDPBaseKey()
//            )
//        }
//
//    private class CoreCallback : CoreCallbackHandler {
//
//        override fun startup(): Long {
//            return 0
//        }
//
//        override fun shutdown(): Long {
//            val serviceControl = serviceControl?.get() ?: return -1
//            return try {
//                serviceControl.stopService()
//                0
//            } catch (e: Exception) {
//                -1
//            }
//        }
//
//        override fun onEmitStatus(l: Long, s: String?): Long {
//            return 0
//        }
//    }


}
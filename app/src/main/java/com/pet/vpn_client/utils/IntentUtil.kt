package com.pet.vpn_client.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pet.vpn_client.app.Constants
import java.io.Serializable

object IntentUtil {

    fun sendMsg2Service(ctx: Context, what: Int, content: Serializable) {
        sendMsg(ctx, Constants.BROADCAST_ACTION_SERVICE, what, content)
    }

    fun sendMsg2UI(ctx: Context, what: Int, content: Serializable) {
        sendMsg(ctx, Constants.BROADCAST_ACTION_ACTIVITY, what, content)
    }

//    fun sendMsg2TestService(ctx: Context, what: Int, content: java.io.Serializable) {
//        try {
//            val intent = Intent()
//            intent.component = ComponentName(ctx, V2RayTestService::class.java)
//            intent.putExtra("key", what)
//            intent.putExtra("content", content)
//            ctx.startService(intent)
//        } catch (e: Exception) {
//            Log.e(AppConfig.TAG, "Failed to send message to test service", e)
//        }
//    }

    private fun sendMsg(ctx: Context, action: String, what: Int, content: Serializable) {
        try {
            val intent = Intent()
            intent.action = action
            intent.`package` = Constants.ANG_PACKAGE
            intent.putExtra("key", what)
            intent.putExtra("content", content)
            ctx.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to send message with action: $action", e)
        }
    }
}
package com.pet.vpn_client.framework.notification

import android.app.PendingIntent
import android.content.Context

interface PendingIntentProvider {
    fun createMainActivityPendingIntent(context: Context): PendingIntent
}
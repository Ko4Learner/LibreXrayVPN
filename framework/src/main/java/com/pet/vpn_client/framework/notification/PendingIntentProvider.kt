package com.pet.vpn_client.framework.notification

import android.app.PendingIntent

interface PendingIntentProvider {
    fun createMainActivityPendingIntent(): PendingIntent
}
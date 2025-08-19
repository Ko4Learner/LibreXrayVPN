package com.pet.vpn_client.framework.notification

import android.app.PendingIntent
import android.content.Context

/**
 * Provides [PendingIntent] instances for navigation and service actions.
 */
interface PendingIntentProvider {
    /**
     * Creates a [PendingIntent] that launches the main activity of the app.
     *
     * @param context Context used for building the PendingIntent (usually application context).
     * @return PendingIntent for opening the main activity.
     */
    fun createMainActivityPendingIntent(context: Context): PendingIntent
}
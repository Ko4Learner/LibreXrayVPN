package org.librexray.vpn.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.librexray.vpn.framework.notification.PendingIntentProvider
import org.librexray.vpn.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI module that provides app-scoped factories for PendingIntent creation.
 *
 * Responsibilities:
 * - Exposes a PendingIntentProvider to construct immutable PendingIntents
 *   used by notifications (e.g., to open MainActivity).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    private const val REQUEST_CODE_OPEN_APP = 0

    /**
     * Provides a factory for building PendingIntents that launch MainActivity.
     */
    @Provides
    @Singleton
    fun providePendingIntentProvider(): PendingIntentProvider = object : PendingIntentProvider {
        /**
         * Creates an immutable PendingIntent that opens MainActivity from a notification.
         */
        override fun createMainActivityPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            return PendingIntent.getActivity(
                context,
                REQUEST_CODE_OPEN_APP,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
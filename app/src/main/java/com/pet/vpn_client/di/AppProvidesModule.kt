package com.pet.vpn_client.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pet.vpn_client.framework.notification.PendingIntentProvider
import com.pet.vpn_client.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    private const val REQUEST_CODE_OPEN_APP = 0

    @Provides
    @Singleton
    fun providePendingIntentProvider(): PendingIntentProvider = object : PendingIntentProvider {
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
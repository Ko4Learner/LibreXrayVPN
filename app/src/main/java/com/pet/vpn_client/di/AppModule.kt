package com.pet.vpn_client.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pet.vpn_client.framework.notification.PendingIntentProvider
import com.pet.vpn_client.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun providePendingIntentProvider(
        @ApplicationContext context: Context
    ): PendingIntentProvider = object : PendingIntentProvider {
        override fun createMainActivityPendingIntent(): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    }
}
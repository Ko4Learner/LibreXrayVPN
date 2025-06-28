package com.pet.vpn_client.di

import android.content.Context
import com.pet.vpn_client.data.SettingsManagerImpl
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.framework.ServiceManagerImpl
import com.pet.vpn_client.framework.bridge.XRayVpnBridge
import com.pet.vpn_client.framework.services.VPNService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FrameworkModule {

    @Provides
    @Singleton
    fun provideServiceManager(
        storage: KeyValueStorage,
        coreVpnBridge: CoreVpnBridge,
        @ApplicationContext context: Context
    ): ServiceManager =
        ServiceManagerImpl(storage, coreVpnBridge, context)

    @Provides
    @Singleton
    fun provideVPNService(
        storage: KeyValueStorage,
        serviceManager: ServiceManager,
        settingsManager: SettingsManagerImpl
    ): VPNService =
        VPNService(storage, serviceManager, settingsManager)

    @Provides
    @Singleton
    fun provideCoreVpnBridge(
        @ApplicationContext context: Context,
        storage: KeyValueStorage,
        serviceManager: ServiceManager,
        configManager: ConfigManager,
        settingsManager: SettingsManagerImpl
    ): CoreVpnBridge =
        XRayVpnBridge(context, storage, serviceManager, configManager, settingsManager)
}
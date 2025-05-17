package com.pet.vpn_client.di

import android.content.Context
import com.pet.vpn_client.data.ConfigManager
import com.pet.vpn_client.data.SettingsManager
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
class FrameworkModule {

    @Provides
    @Singleton
    fun provideServiceManager(
        storage: KeyValueStorage,
        coreVpnBridge: CoreVpnBridge
    ): ServiceManager =
        ServiceManagerImpl(storage, coreVpnBridge)

    @Provides
    @Singleton
    fun provideVPNService(storage: KeyValueStorage, serviceManager: ServiceManager): VPNService =
        VPNService(storage, serviceManager)

    @Provides
    @Singleton
    fun provideCoreVpnBridge(
        @ApplicationContext context: Context,
        storage: KeyValueStorage,
        serviceManager: ServiceManager,
        configManager: ConfigManager,
        settingsManager: SettingsManager
    ): CoreVpnBridge =
        XRayVpnBridge(context, storage, serviceManager, configManager, settingsManager)
}
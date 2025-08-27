package com.pet.vpn_client.framework.vpn_service_test

import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.framework.di.FrameworkBindsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FrameworkBindsModule::class]
)
object VpnServiceTestModule {

    @Provides
    @Singleton
    fun provideServiceManager(): ServiceManager = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideServiceStateRepository(): ServiceStateRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideConnectionInteractor(): ConnectionInteractor = mockk {
        every { observeSpeed() } returns emptyFlow()
    }

    @Provides
    @Singleton
    fun provideCoreVpnBridge(): CoreVpnBridge = mockk(relaxed = true)
}
package org.librexray.vpn.framework.vpn_service_test

import org.librexray.vpn.domain.interfaces.CoreVpnBridge
import org.librexray.vpn.domain.interfaces.ServiceManager
import org.librexray.vpn.domain.interfaces.interactor.ConnectionInteractor
import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.framework.di.FrameworkBindsModule
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
package org.librexray.vpn.framework.di

import org.librexray.vpn.domain.interactor_impl.ConnectionInteractorImpl
import org.librexray.vpn.domain.interfaces.CoreVpnBridge
import org.librexray.vpn.domain.interfaces.ServiceManager
import org.librexray.vpn.domain.interfaces.interactor.ConnectionInteractor
import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.framework.ServiceManagerImpl
import org.librexray.vpn.framework.ServiceStateRepositoryImpl
import org.librexray.vpn.framework.bridge_to_core.XRayVpnBridge
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FrameworkBindsModule {
    @Binds
    @Singleton
    abstract fun bindConnectionInteractor(impl: ConnectionInteractorImpl): ConnectionInteractor

    @Binds
    @Singleton
    abstract fun bindServiceManager(impl: ServiceManagerImpl): ServiceManager

    @Binds
    @Singleton
    abstract fun bindCoreVpnBridge(impl: XRayVpnBridge): CoreVpnBridge

    @Binds
    @Singleton
    abstract fun bindServiceStateRepository(impl: ServiceStateRepositoryImpl): ServiceStateRepository
}
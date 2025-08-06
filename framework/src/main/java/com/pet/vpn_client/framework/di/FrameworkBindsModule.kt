package com.pet.vpn_client.framework.di

import com.pet.vpn_client.domain.interactor_impl.ConnectionInteractorImpl
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.framework.ServiceManagerImpl
import com.pet.vpn_client.framework.ServiceStateRepositoryImpl
import com.pet.vpn_client.framework.bridge.XRayVpnBridge
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
package com.pet.vpn_client.di

import com.pet.vpn_client.domain.interactor_impl.ConfigInteractorImpl
import com.pet.vpn_client.domain.interactor_impl.ConnectionInteractorImpl
import com.pet.vpn_client.domain.interactor_impl.SettingsInteractorImpl
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.SubscriptionManager
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.interfaces.interactor.ConnectionInteractor
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideConfigInteractor(
        subscriptionManager: SubscriptionManager,
        keyValueStorage: KeyValueStorage
    ): ConfigInteractor = ConfigInteractorImpl(subscriptionManager, keyValueStorage)

    @Provides
    @Singleton
    fun provideConnectionInteractor(serviceManager: ServiceManager): ConnectionInteractor =
        ConnectionInteractorImpl(serviceManager)

    @Provides
    @Singleton
    fun provideSettingsInteractor(keyValueStorage: KeyValueStorage): SettingsInteractor =
        SettingsInteractorImpl(keyValueStorage)
}
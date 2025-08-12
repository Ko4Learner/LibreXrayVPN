package com.pet.vpn_client.data.di

import com.pet.vpn_client.data.mmkv.MMKVStorage
import com.pet.vpn_client.data.repository_impl.SettingsRepositoryImpl
import com.pet.vpn_client.data.repository_impl.ServerConfigImportRepositoryImpl
import com.pet.vpn_client.domain.interactor_impl.ConfigInteractorImpl
import com.pet.vpn_client.domain.interactor_impl.SettingsInteractorImpl
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
import com.pet.vpn_client.domain.interfaces.repository.ServerConfigImportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    @Singleton
    abstract fun bindConfigInteractor(impl: ConfigInteractorImpl): ConfigInteractor

    @Binds
    @Singleton
    abstract fun bindSettingsInteractor(impl: SettingsInteractorImpl): SettingsInteractor

    @Binds
    @Singleton
    abstract fun bindMMKVConfig(impl: MMKVStorage): KeyValueStorage

    @Binds
    @Singleton
    abstract fun bindServerConfigImportRepository(impl: ServerConfigImportRepositoryImpl): ServerConfigImportRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
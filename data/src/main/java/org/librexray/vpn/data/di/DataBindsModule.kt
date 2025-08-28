package org.librexray.vpn.data.di

import org.librexray.vpn.data.mmkv.MMKVStorage
import org.librexray.vpn.data.repository_impl.SettingsRepositoryImpl
import org.librexray.vpn.data.repository_impl.ServerConfigImportRepositoryImpl
import org.librexray.vpn.domain.interactor_impl.ConfigInteractorImpl
import org.librexray.vpn.domain.interactor_impl.SettingsInteractorImpl
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.interfaces.interactor.SettingsInteractor
import org.librexray.vpn.domain.interfaces.repository.SettingsRepository
import org.librexray.vpn.domain.interfaces.repository.ServerConfigImportRepository
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
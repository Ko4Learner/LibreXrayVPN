package com.pet.vpn_client.data.di

import android.content.Context
import com.google.gson.Gson
import com.pet.vpn_client.data.config_formatter.HttpFormatter
import com.pet.vpn_client.data.config_formatter.ShadowsocksFormatter
import com.pet.vpn_client.data.config_formatter.SocksFormatter
import com.pet.vpn_client.data.config_formatter.TrojanFormatter
import com.pet.vpn_client.data.config_formatter.VlessFormatter
import com.pet.vpn_client.data.config_formatter.VmessFormatter
import com.pet.vpn_client.data.config_formatter.WireguardFormatter
import com.pet.vpn_client.data.mmkv.MMKVStorage
import com.pet.vpn_client.data.repository_impl.ConfigRepositoryImpl
import com.pet.vpn_client.data.repository_impl.SettingsRepositoryImpl
import com.pet.vpn_client.data.repository_impl.SubscriptionRepositoryImpl
import com.pet.vpn_client.domain.interactor_impl.ConfigInteractorImpl
import com.pet.vpn_client.domain.interactor_impl.SettingsInteractorImpl
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
import com.pet.vpn_client.domain.interfaces.repository.SubscriptionRepository
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideConfigInteractor(
        subscriptionRepository: SubscriptionRepository,
        keyValueStorage: KeyValueStorage
    ): ConfigInteractor = ConfigInteractorImpl(subscriptionRepository, keyValueStorage)


    @Provides
    @Singleton
    fun provideSettingsInteractor(
        settingsRepository: SettingsRepository
    ): SettingsInteractor =
        SettingsInteractorImpl(settingsRepository)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()


    @Provides
    @Singleton
    fun provideMMKVConfig(gson: Gson): KeyValueStorage = MMKVStorage(gson)

    @Provides
    @Singleton
    fun provideConfigManager(
        storage: KeyValueStorage,
        gson: Gson,
        httpFormatter: HttpFormatter,
        shadowsocksFormatter: ShadowsocksFormatter,
        socksFormatter: SocksFormatter,
        trojanFormatter: TrojanFormatter,
        vlessFormatter: VlessFormatter,
        vmessFormatter: VmessFormatter,
        wireguardFormatter: WireguardFormatter,
        @ApplicationContext context: Context
    ): ConfigRepository =
        ConfigRepositoryImpl(
            storage,
            gson,
            httpFormatter,
            shadowsocksFormatter,
            socksFormatter,
            trojanFormatter,
            vlessFormatter,
            vmessFormatter,
            wireguardFormatter,
            context
        )

    @Provides
    @Singleton
    fun provideSubscriptionManager(
        storage: KeyValueStorage,
        gson: Gson,
        configRepository: ConfigRepository,
        shadowsocksFormatter: ShadowsocksFormatter,
        socksFormatter: SocksFormatter,
        trojanFormatter: TrojanFormatter,
        vlessFormatter: VlessFormatter,
        vmessFormatter: VmessFormatter,
        wireguardFormatter: WireguardFormatter,
        @ApplicationContext context: Context
    ): SubscriptionRepository = SubscriptionRepositoryImpl(
        storage,
        gson,
        configRepository,
        shadowsocksFormatter,
        socksFormatter,
        trojanFormatter,
        vlessFormatter,
        vmessFormatter,
        wireguardFormatter,
        context
    )

    @Provides
    @Singleton
    fun provideSettingsRepository(storage: KeyValueStorage, gson: Gson): SettingsRepository =
        SettingsRepositoryImpl(storage, gson)
}
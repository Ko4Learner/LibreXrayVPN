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
import com.pet.vpn_client.data.repository_impl.ConfigManagerImpl
import com.pet.vpn_client.data.repository_impl.SettingsManagerImpl
import com.pet.vpn_client.data.repository_impl.SubscriptionManagerImpl
import com.pet.vpn_client.domain.interactor_impl.ConfigInteractorImpl
import com.pet.vpn_client.domain.interactor_impl.SettingsInteractorImpl
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.domain.interfaces.SubscriptionManager
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
        subscriptionManager: SubscriptionManager,
        keyValueStorage: KeyValueStorage
    ): ConfigInteractor = ConfigInteractorImpl(subscriptionManager, keyValueStorage)


    @Provides
    @Singleton
    fun provideSettingsInteractor(
        settingsManager: SettingsManager
    ): SettingsInteractor =
        SettingsInteractorImpl(settingsManager)

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
    ): ConfigManager =
        ConfigManagerImpl(
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
        configManager: ConfigManager,
        shadowsocksFormatter: ShadowsocksFormatter,
        socksFormatter: SocksFormatter,
        trojanFormatter: TrojanFormatter,
        vlessFormatter: VlessFormatter,
        vmessFormatter: VmessFormatter,
        wireguardFormatter: WireguardFormatter,
        @ApplicationContext context: Context
    ): SubscriptionManager = SubscriptionManagerImpl(
        storage,
        gson,
        configManager,
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
    fun provideSettingsManager(storage: KeyValueStorage, gson: Gson): SettingsManager =
        SettingsManagerImpl(storage, gson)
}
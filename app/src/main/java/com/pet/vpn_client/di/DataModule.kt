package com.pet.vpn_client.di

import com.google.gson.Gson
import com.pet.vpn_client.data.ConfigManager
import com.pet.vpn_client.data.SettingsManager
import com.pet.vpn_client.data.config_formatter.HttpFormatter
import com.pet.vpn_client.data.mmkv.MMKVStorage
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    //@ApplicationContext context: Context

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
        settingsManager: SettingsManager,
        httpFormatter: HttpFormatter
    ): ConfigManager =
        ConfigManager(storage, gson, settingsManager, httpFormatter)

    @Provides
    @Singleton
    fun provideHttpFormatter(configManager: ConfigManager): HttpFormatter =
        HttpFormatter(configManager)
}
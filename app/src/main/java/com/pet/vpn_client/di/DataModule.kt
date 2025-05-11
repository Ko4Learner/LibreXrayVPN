package com.pet.vpn_client.di

import com.google.gson.Gson
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
}
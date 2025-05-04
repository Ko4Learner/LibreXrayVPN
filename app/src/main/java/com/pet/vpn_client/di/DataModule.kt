package com.pet.vpn_client.di

import android.content.Context
import com.google.gson.Gson
import com.pet.vpn_client.data.mmkv.MMKVConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

//    @Provides
//    @Singleton
//    fun provide___Repository(@ApplicationContext context: Context): ___Repository {
//        return ___RepositoryImpl()
//    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideMMKVConfig(gson: Gson): MMKVConfig {
        return MMKVConfig(gson)
    }

}
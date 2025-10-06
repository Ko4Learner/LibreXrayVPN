package org.librexray.vpn.presentation.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Qualifier for the I/O coroutine dispatcher used in the presentation layer.
 *
 * Rationale:
 * - Centralizes dispatcher injection so tests can replace it with a TestDispatcher.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Provides coroutine dispatchers for the app.
 *
 * Testing:
 * - Replace this binding with a test module and provide a StandardTestDispatcher
 *   under the same @IoDispatcher qualifier to gain control over virtual time.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
package com.pet.vpn_client.domain.interfaces.interactor

import com.pet.vpn_client.domain.models.AppLocale
import com.pet.vpn_client.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Domain-level access to user-configurable application settings.
 *
 * Contracts:
 * - `setLocale(locale)`: persists the preferred locale tag (e.g., `"en-US"`, `"ru"`, or `"auto"` for system default).
 * - `observeLocale()`: **hot** stream; immediately emits the current locale and updates on changes.
 * - `setTheme(theme)`: persists the preferred [ThemeMode] (e.g., light, dark, system).
 * - `observeTheme()`: **hot** stream; immediately emits the current theme and updates on changes.
 */
interface SettingsInteractor {
    fun observeLocale(): Flow<Locale>
    suspend fun setLocale(locale: AppLocale)
    fun observeTheme(): Flow<ThemeMode>
    suspend fun setTheme(theme: ThemeMode)
}
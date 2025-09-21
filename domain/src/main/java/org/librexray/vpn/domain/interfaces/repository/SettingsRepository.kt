package org.librexray.vpn.domain.interfaces.repository

import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Central access point for user-facing application settings (locale, theme, etc.).
 *
 * Contracts:
 * - observeLocale(): hot stream that emits the current locale immediately and updates on change.
 * - setLocale(locale): persists a preferred locale (enum-based) and notifies observers.
 * - getLocale(): returns the current locale synchronously (fast, from memory/cache).
 * - observeTheme(): hot stream that emits the current theme mode immediately and updates on change.
 * - setTheme(theme): persists a preferred theme mode (enum-based) and notifies observers.
 */
interface SettingsRepository {
    fun observeLocale(): Flow<AppLocale>
    suspend fun setLocale(locale: AppLocale)
    fun getLocale(): AppLocale
    fun observeTheme(): Flow<ThemeMode>
    suspend fun setTheme(theme: ThemeMode)
}
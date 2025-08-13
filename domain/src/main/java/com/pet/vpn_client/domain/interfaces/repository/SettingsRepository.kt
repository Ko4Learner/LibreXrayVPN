package com.pet.vpn_client.domain.interfaces.repository

import com.pet.vpn_client.domain.models.AppLocale
import com.pet.vpn_client.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.util.Locale

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
    fun observeLocale(): Flow<Locale>
    suspend fun setLocale(locale: AppLocale)
    fun getLocale(): Locale
    fun observeTheme(): Flow<ThemeMode>
    suspend fun setTheme(theme: ThemeMode)
}
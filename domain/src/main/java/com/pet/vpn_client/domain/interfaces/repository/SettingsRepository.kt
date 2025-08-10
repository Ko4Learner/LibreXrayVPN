package com.pet.vpn_client.domain.interfaces.repository

import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Central access point for user-facing application settings (locale, theme, etc.).
 * Contracts (current):
 * - observeLocale(): hot stream that emits the current locale immediately and updates on change.
 * - getLocale(): returns the current locale synchronously (fast, from memory/cache).
 * - setLocale(localeTag): persists a preferred locale given as a BCP-47 tag (e.g., "en-US", "ru", "auto"),
 *   normalizes the value and triggers an update for observers.
 */
interface SettingsRepository {
    fun observeLocale(): Flow<Locale>
    fun getLocale(): Locale
    suspend fun setLocale(localeTag: String)
}
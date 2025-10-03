package org.librexray.vpn.domain.interfaces.interactor

import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level access to user-configurable application settings.
 *
 * Contracts:
 * - `setLocale(locale)`: persists the preferred locale tag (e.g., `"en-US"`, `"ru"`, or `"auto"` for system default).
 * - `observeLocale()`: **hot** stream; immediately emits the current locale and updates on changes.
 * - `setTheme(theme)`: persists the preferred [ThemeMode] (e.g., light, dark, system).
 * - `observeTheme()`: **hot** stream; immediately emits the current theme and updates on changes.
 * - wasNotificationAsked(): returns a boolean flag indicating whether the app has already prompted
 *   for the notifications permission. Used to avoid prompting multiple times.
 * - markNotificationAsked(): marks the notifications permission as requested by persisting `true`.
 */
interface SettingsInteractor {
    fun observeLocale(): Flow<AppLocale>
    suspend fun setLocale(locale: AppLocale)
    fun observeTheme(): Flow<ThemeMode>
    suspend fun setTheme(theme: ThemeMode)
    fun wasNotificationAsked(): Boolean
    suspend fun markNotificationAsked()
}
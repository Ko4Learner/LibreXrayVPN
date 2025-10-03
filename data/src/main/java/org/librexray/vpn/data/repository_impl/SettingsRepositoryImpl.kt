package org.librexray.vpn.data.repository_impl

import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.interfaces.repository.SettingsRepository
import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Default implementation of [SettingsRepository].
 */
class SettingsRepositoryImpl @Inject constructor(private val storage: KeyValueStorage) :
    SettingsRepository {
    /**
     * Hot stream emitting the current [AppLocale].
     */
    private val _localeFlow = MutableStateFlow(AppLocale.fromTag(storage.decodeSettingsString(LANGUAGE)))

    /**
     * Hot stream emitting the current theme mode.
     */
    private val _themeFlow =
        MutableStateFlow(ThemeMode.fromTag(storage.decodeSettingsString(THEME)))

    /**
     * Observes the user's preferred locale.
     */
    override fun observeLocale(): Flow<AppLocale> = _localeFlow

    /**
     * Persists the preferred locale tag and updates observers.
     */
    override suspend fun setLocale(locale: AppLocale) {
        storage.encodeSettingsString(LANGUAGE, locale.toTag())
        _localeFlow.value = locale
    }

    /**
     * Observes the user's preferred theme mode.
     */
    override fun observeTheme(): Flow<ThemeMode> = _themeFlow

    /**
     * Persists the preferred theme mode and updates observers.
     */
    override suspend fun setTheme(theme: ThemeMode) {
        storage.encodeSettingsString(THEME, theme.toTag())
        _themeFlow.value = theme
    }

    /**
     * Returns whether the app has already asked for the notifications permission.
     * Used to avoid prompting the user more than once.
     */
    override fun wasNotificationAsked(): Boolean {
        return storage.decodeSettingsBoolean(NOTIFICATION_PERMISSION)
    }

    /**
     * Persists a marker that the notifications permission has been requested.
     */
    override suspend fun markNotificationAsked() {
        storage.encodeSettingsBoolean(NOTIFICATION_PERMISSION, true)
    }

    companion object {
        private const val LANGUAGE = "language"
        private const val THEME = "theme"
        private const val NOTIFICATION_PERMISSION = "notification_permission"
    }
}
package org.librexray.vpn.data.repository_impl

import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.interfaces.repository.SettingsRepository
import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import javax.inject.Inject

/**
 * Default implementation of [SettingsRepository].
 */
class SettingsRepositoryImpl @Inject constructor(private val storage: KeyValueStorage) :
    SettingsRepository {
    /**
     * Hot stream emitting the current [Locale].
     */
    private val _localeFlow = MutableStateFlow(getLocale())

    /**
     * Hot stream emitting the current theme mode.
     */
    private val _themeFlow =
        MutableStateFlow(ThemeMode.fromTag(storage.decodeSettingsString(THEME)))

    /**
     * Observes the user's preferred locale.
     */
    override fun observeLocale(): Flow<Locale> = _localeFlow

    /**
     * Persists the preferred locale tag and updates observers.
     */
    override suspend fun setLocale(locale: AppLocale) {
        storage.encodeSettingsString(LANGUAGE, locale.toTag())
        _localeFlow.value = resolveEffectiveLocale(locale)
    }

    /**
     * Returns the current effective [Locale].
     */
    override fun getLocale(): Locale =
        resolveEffectiveLocale(AppLocale.fromTag(storage.decodeSettingsString(LANGUAGE)))

    /**
     * Maps an [AppLocale] selection to a concrete [Locale].
     *
     * - [AppLocale.SYSTEM]: returns Russian for system `ru`, English for system `en`,
     *   otherwise English (fallback).
     */
    private fun resolveEffectiveLocale(mode: AppLocale): Locale = when (mode) {
        AppLocale.SYSTEM -> when (Locale.getDefault().language.lowercase(Locale.ROOT)) {
            Constants.RU_LOCALE_TAG -> Locale.forLanguageTag(Constants.RU_LOCALE_TAG)
            Constants.EN_LOCALE_TAG -> Locale.forLanguageTag(Constants.EN_LOCALE_TAG)
            else -> Locale.forLanguageTag(Constants.EN_LOCALE_TAG)
        }

        AppLocale.RU -> Locale.forLanguageTag(Constants.RU_LOCALE_TAG)
        AppLocale.EN -> Locale.forLanguageTag(Constants.EN_LOCALE_TAG)
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

    companion object {
        private const val LANGUAGE = "language"
        private const val THEME = "theme"
    }
}
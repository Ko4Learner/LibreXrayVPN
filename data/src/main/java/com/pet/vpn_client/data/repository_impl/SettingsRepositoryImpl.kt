package com.pet.vpn_client.data.repository_impl

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
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
     * Observes the user's preferred locale.
     */
    override fun observeLocale(): Flow<Locale> = _localeFlow

    /**
     * Persists the preferred locale tag and updates observers.
     */
    override suspend fun setLocale(localeTag: String) {
        storage.encodeSettingsString(PREF_LANGUAGE, localeTag)
        _localeFlow.value = getLocale()
    }

    /**
     * Returns the current effective [Locale].
     */
    override fun getLocale(): Locale {
        val tag = storage.decodeSettingsString(PREF_LANGUAGE)
        return when {
            tag.isNullOrEmpty() || tag == Constants.AUTO_LOCALE_TAG -> Locale.getDefault()
            else -> Locale.forLanguageTag(tag)
        }
    }

    companion object {
        private const val PREF_LANGUAGE = "pref_language"
    }
}
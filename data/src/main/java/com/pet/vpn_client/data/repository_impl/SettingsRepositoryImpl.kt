package com.pet.vpn_client.data.repository_impl

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(val storage: KeyValueStorage) :
    SettingsRepository {
    private val _localeFlow = MutableStateFlow(getLocale())
    override fun observeLocale(): Flow<Locale> = _localeFlow

    override suspend fun setLocale(localeTag: String) {
        storage.encodeSettings(PREF_LANGUAGE, localeTag)
        _localeFlow.value = getLocale()
    }

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
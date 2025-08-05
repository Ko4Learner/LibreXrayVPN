package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {
    override suspend fun setLocale(localeTag: String) {
        settingsRepository.setLocale(localeTag)
    }

    override fun observeLocale(): Flow<Locale> = settingsRepository.observeLocale()
}
package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.repository.SettingsRepository
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import com.pet.vpn_client.domain.models.AppLocale
import com.pet.vpn_client.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {
    override fun observeLocale(): Flow<Locale> = settingsRepository.observeLocale()
    override suspend fun setLocale(locale: AppLocale) = settingsRepository.setLocale(locale)
    override fun observeTheme(): Flow<ThemeMode> = settingsRepository.observeTheme()
    override suspend fun setTheme(theme: ThemeMode) = settingsRepository.setTheme(theme)
}
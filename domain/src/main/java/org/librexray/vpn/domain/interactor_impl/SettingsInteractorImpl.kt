package org.librexray.vpn.domain.interactor_impl

import org.librexray.vpn.domain.interfaces.repository.SettingsRepository
import org.librexray.vpn.domain.interfaces.interactor.SettingsInteractor
import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {
    override fun observeLocale(): Flow<AppLocale> = settingsRepository.observeLocale()
    override suspend fun setLocale(locale: AppLocale) = settingsRepository.setLocale(locale)
    override fun observeTheme(): Flow<ThemeMode> = settingsRepository.observeTheme()
    override suspend fun setTheme(theme: ThemeMode) = settingsRepository.setTheme(theme)
    override fun wasNotificationAsked(): Boolean = settingsRepository.wasNotificationAsked()
    override suspend fun markNotificationAsked() = settingsRepository.markNotificationAsked()
}
package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsManager: SettingsManager
) : SettingsInteractor {
    override suspend fun setLocale(localeTag: String) {
        settingsManager.setLocale(localeTag)
    }

    override fun observeLocale(): Flow<Locale> = settingsManager.observeLocale()
}
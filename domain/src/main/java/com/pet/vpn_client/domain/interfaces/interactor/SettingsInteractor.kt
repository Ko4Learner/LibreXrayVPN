package com.pet.vpn_client.domain.interfaces.interactor

import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Domain access to application settings that affect behavior and UI.
 *
 * Contracts:
 * - setLocale(localeTag): persists a tag (e.g., "en-US", "ru", "auto");
 * - observeLocale(): **hot** stream that immediately emits the current value on subscription and updates on changes.
 */
interface SettingsInteractor {
    suspend fun setLocale(localeTag: String)
    fun observeLocale(): Flow<Locale>
}
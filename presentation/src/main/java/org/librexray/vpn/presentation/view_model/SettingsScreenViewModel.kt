package org.librexray.vpn.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.librexray.vpn.domain.interfaces.interactor.SettingsInteractor
import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import org.librexray.vpn.presentation.intent.SettingsScreenIntent
import org.librexray.vpn.presentation.state.SettingsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.librexray.vpn.presentation.di.IoDispatcher
import javax.inject.Inject

/**
 * State holder for the Settings screen.
 *
 * Responsibilities:
 * - Observes and updates app-level preferences (locale, theme).
 * - Exposes immutable [state] for the UI to render.
 *
 * Threading:
 * - Writes are dispatched on [io]; reads are collected on the Main thread.
 */
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observeLocale().collect { locale ->
                _state.update { it.copy(localeMode = locale) }
            }
        }
        viewModelScope.launch {
            settingsInteractor.observeTheme().collect { themeMode ->
                _state.update { it.copy(themeMode = themeMode) }
            }
        }
    }

    /**
     * Entry point for user intents from the Settings screen.
     */
    fun onIntent(intent: SettingsScreenIntent) {
        when (intent) {
            is SettingsScreenIntent.SetLocale -> setLocale(intent.localeMode)
            is SettingsScreenIntent.SetTheme -> setTheme(intent.themeMode)
        }
    }

    /** Persists the selected locale asynchronously. */
    private fun setLocale(locale: AppLocale) {
        viewModelScope.launch(io) {
            settingsInteractor.setLocale(locale)
        }
    }

    /** Persists the selected theme asynchronously. */
    private fun setTheme(themeMode: ThemeMode) {
        viewModelScope.launch(io) {
            settingsInteractor.setTheme(themeMode)
        }
    }
}
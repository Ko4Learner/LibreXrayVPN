package com.pet.vpn_client.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import com.pet.vpn_client.domain.models.AppLocale
import com.pet.vpn_client.domain.models.ThemeMode
import com.pet.vpn_client.presentation.intent.SettingsScreenIntent
import com.pet.vpn_client.presentation.state.SettingsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(private val settingsInteractor: SettingsInteractor) :
    ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observeLocale().collect { locale ->
                _state.update { it.copy(locale = locale) }
            }
        }
        viewModelScope.launch {
            settingsInteractor.observeTheme().collect { themeMode ->
                _state.update { it.copy(themeMode = themeMode) }
            }
        }
    }

    fun intent(intent: SettingsScreenIntent) {
        when (intent) {
            is SettingsScreenIntent.SetLocale -> setLocale(intent.locale)
            is SettingsScreenIntent.SetTheme -> setTheme(intent.themeMode)
        }
    }

    private fun setLocale(locale: AppLocale) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsInteractor.setLocale(locale)
        }
    }

    private fun setTheme(themeMode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsInteractor.setTheme(themeMode)
        }
    }
}
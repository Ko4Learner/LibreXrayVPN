package com.pet.vpn_client.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.domain.interfaces.interactor.SettingsInteractor
import com.pet.vpn_client.presentation.state.SettingsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(private val settingsInteractor: SettingsInteractor) :
    ViewModel() {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observeLocale().collect {
                _state.value = _state.value.copy(locale = it)
            }
        }
    }

    private suspend fun setLocale(tag: String) {
        settingsInteractor.setLocale(tag)
    }
}
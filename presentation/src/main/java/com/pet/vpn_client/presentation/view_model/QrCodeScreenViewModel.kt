package com.pet.vpn_client.presentation.view_model

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.presentation.formatter.toFrameData
import com.pet.vpn_client.presentation.intent.QrCodeScreenIntent
import com.pet.vpn_client.presentation.state.QrCodeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor
) : ViewModel() {
    private val _state = MutableStateFlow(QrCodeScreenState())
    val state: StateFlow<QrCodeScreenState> = _state.asStateFlow()

    fun onIntent(intent: QrCodeScreenIntent) {
        when (intent) {
            QrCodeScreenIntent.ResetState -> _state.update {
                it.copy(
                    configFound = false,
                    error = null
                )
            }
        }
    }

    //TODO выглядит как костыль + нужно сбрасывать при пересоздании экрана
    private var hasProcessed = false
    fun onAnalyzeFrame(imageProxy: ImageProxy) {
        if (hasProcessed) {
            imageProxy.close()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (configInteractor.importQrCodeConfig(imageProxy.toFrameData())) {
                    -1 -> _state.update {
                        it.copy(
                            configFound = false,
                            error = null
                        )
                    }

                    0 ->
                        _state.update {
                            it.copy(
                                configFound = false,
                                error = "Конфигурация не найдена"
                            )
                        }

                    else -> {
                        hasProcessed = true
                        _state.update {
                            it.copy(
                                configFound = true,
                                error = null
                            )
                        }
                    }
                }
            } finally {
                imageProxy.close()
            }
        }
    }
}

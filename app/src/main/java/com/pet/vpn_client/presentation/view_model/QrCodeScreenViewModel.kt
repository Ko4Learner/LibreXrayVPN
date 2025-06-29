package com.pet.vpn_client.presentation.view_model

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.presentation.formatter.toFrameData
import com.pet.vpn_client.presentation.intent.QrCodeScreenIntent
import com.pet.vpn_client.presentation.state.QrCodeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor
) :
    ViewModel() {

    private val _state = MutableStateFlow(QrCodeScreenState())
    val state: StateFlow<QrCodeScreenState> = _state.asStateFlow()

    fun onEvent(event: QrCodeScreenIntent) {
        when (event) {
            is QrCodeScreenIntent.ScanFromGallery -> {}
//                {
//                viewModelScope.launch {
//                    _state.update { it.copy(isLoading = true) }
//                    val result = scanQrFromGallery(event.uri)
//                    result.fold(
//                        onSuccess = {
//                            _state.update { it.copy(isLoading = false, result = it) }
//                        },
//                        onFailure = {
//                            _state.update { it.copy(isLoading = false, error = it.message) }
//                        }
//                    )
//                }
//            }

            is QrCodeScreenIntent.OnQrScanned -> {
                _state.update { it.copy(result = event.result) }
            }

            is QrCodeScreenIntent.OnScanFailed -> {
                _state.update { it.copy(error = event.reason) }
            }

            is QrCodeScreenIntent.ClearResult -> {
                _state.value = QrCodeScreenState()
            }

            else -> {}
        }
    }

    fun onAnalyzeFrame(imageProxy: ImageProxy) {
        viewModelScope.launch {
            if (configInteractor.importQrCodeConfig(imageProxy.toFrameData()) >= 0) {
                _state.update { it.copy(isQrCodeDetected = true) }
            }
        }
    }
}

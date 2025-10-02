package org.librexray.vpn.presentation.view_model

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.models.ImportResult
import org.librexray.vpn.presentation.formatter.toFrameData
import org.librexray.vpn.presentation.intent.QrCodeScreenIntent
import org.librexray.vpn.presentation.state.QrCodeScreenState
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
    private var hasProcessed = false

    fun onIntent(intent: QrCodeScreenIntent) {
        when (intent) {
            QrCodeScreenIntent.ResetState -> _state.update {
                it.copy(
                    configFound = false,
                    error = false
                )
            }
        }
    }

    fun onAnalyzeFrame(imageProxy: ImageProxy) {
        if (hasProcessed) {
            imageProxy.close()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (configInteractor.importQrCodeConfig(imageProxy.toFrameData())) {
                    ImportResult.Error -> _state.update {
                        it.copy(
                            configFound = false,
                            error = true
                        )
                    }

                    ImportResult.Empty ->
                        _state.update {
                            it.copy(
                                configFound = false,
                                error = false
                            )
                        }

                    else -> {
                        hasProcessed = true
                        _state.update {
                            it.copy(
                                configFound = true,
                                error = false
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

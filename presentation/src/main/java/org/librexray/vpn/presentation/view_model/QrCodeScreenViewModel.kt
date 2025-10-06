package org.librexray.vpn.presentation.view_model

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.models.ImportResult
import org.librexray.vpn.presentation.mapper.toFrameData
import org.librexray.vpn.presentation.intent.QrCodeScreenIntent
import org.librexray.vpn.presentation.state.QrCodeScreenState
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
 * State holder for QR code scanning and import flow.
 *
 * Responsibilities:
 * - Analyzes camera frames and attempts to import VPN configs.
 * - Exposes [QrCodeScreenState] with flags for found/error states.
 *
 * Frame processing contract:
 * - Only the first successful import is processed; further frames are ignored
 *   until the state is reset via [QrCodeScreenIntent.ResetState].
 * - The caller of [onAnalyzeFrame] is responsible for providing [ImageProxy]
 *   frames; this ViewModel ensures each frame is closed in a finally block.
 */
@HiltViewModel
class QrCodeScreenViewModel @Inject constructor(
    private val configInteractor: ConfigInteractor,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow(QrCodeScreenState())
    val state: StateFlow<QrCodeScreenState> = _state.asStateFlow()

    /** Prevents reprocessing frames after a successful import. */
    private var hasProcessed = false

    /**
     * Handles UI intents for the QR screen.
     * Currently supports only state reset.
     */
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

    /**
     * Processes a single camera frame. Converts it to [FrameData] and tries to import config.
     * Guarantees the frame is closed exactly once.
     */
    fun onAnalyzeFrame(imageProxy: ImageProxy) {
        if (hasProcessed) {
            imageProxy.close()
            return
        }
        viewModelScope.launch(io) {
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

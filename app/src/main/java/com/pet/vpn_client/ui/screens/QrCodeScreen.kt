package com.pet.vpn_client.ui.screens

import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pet.vpn_client.presentation.state.QrCodeScreenState
import com.pet.vpn_client.ui.composable_elements.CameraView

@Composable
fun QrCodeScreen(
//    modifier: Modifier = Modifier,
//    onResult: () -> Unit,
    //viewModel: VpnScreenViewModel = hiltViewModel(),
    state: QrCodeScreenState,
//    onEvent: (QrCodeScreenIntent) -> Unit,
    onAnalyze: (ImageProxy) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraView(
            modifier = Modifier.fillMaxSize(),
            onFrame = onAnalyze
        )

        if (state.result != null) {
            Text(
                text = "Результат: ${state.result}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = Color.Green
            )
        }

        if (state.error != null) {
            Text(
                text = "Ошибка: ${state.error}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = Color.Red
            )
        }

        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center).takeIf { state.isLoading } ?: Modifier
        )
    }
}
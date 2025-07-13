package com.pet.vpn_client.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.intent.QrCodeScreenIntent
import com.pet.vpn_client.presentation.view_model.QrCodeScreenViewModel
import com.pet.vpn_client.ui.composable_elements.CameraView

@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: QrCodeScreenViewModel = hiltViewModel(),
    onResult: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Box(modifier = modifier.fillMaxSize()) {
        CameraView(
            modifier = Modifier.fillMaxSize(),
            onFrame = viewModel::onAnalyzeFrame
        )
        //TODO разрешения

        if (state.error != null) {
            Text(
                text = "Ошибка: ${state.error}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = Color.Red
            )
        }
    }

    LaunchedEffect(state.configFound) {
        //TODO возможно необходимо обработать UX считывания конфигурации или возможных ошибок
        if (state.configFound) {
            viewModel.onIntent(QrCodeScreenIntent.ResetState)
            onResult()
        }
    }
}
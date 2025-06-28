package com.pet.vpn_client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel

@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    onResult: () -> Unit,
    viewModel: VpnScreenViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Выберите источник")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onResult() }) {
            Text("Сканировать с камеры")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {  onResult() }) {
            Text("Выбрать изображение")
        }
    }
}
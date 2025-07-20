package com.pet.vpn_client.presentation.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pet.vpn_client.presentation.intent.VpnScreenIntent
import com.pet.vpn_client.presentation.state.VpnScreenState
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel
import com.pet.vpn_client.presentation.composable_elements.ConfigDropDownMenu
import com.pet.vpn_client.presentation.composable_elements.RestartButton
import com.pet.vpn_client.presentation.composable_elements.TestConnectionButton
import com.pet.vpn_client.presentation.composable_elements.StartVpnButton
import com.pet.vpn_client.presentation.composable_elements.SubscriptionsList
import com.pet.vpn_client.presentation.composable_elements.SwitchVpnProxy

@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onQrCodeClick: () -> Unit,
    viewModel: VpnScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val qrCodeImported =
        savedStateHandle?.getStateFlow<Boolean>("qrCodeImported", false)?.collectAsState()

    LaunchedEffect(qrCodeImported?.value) {
        if (qrCodeImported?.value == true) {
            viewModel.onIntent(VpnScreenIntent.RefreshItemList)
            savedStateHandle.remove<Boolean>("qrCodeImported")
        }
    }

    VpnScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        onQrCodeClick = onQrCodeClick
    )
}

@Composable
fun VpnScreenContent(
    modifier: Modifier = Modifier,
    state: VpnScreenState,
    onIntent: (VpnScreenIntent) -> Unit,
    onQrCodeClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
            .background(colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitchVpnProxy(onIntent, state.isVpnMode)
            ConfigDropDownMenu(onIntent, onQrCodeClick)
        }

        Column(modifier = Modifier.weight(1f)) {
            SubscriptionsList(onIntent, state.serverItemList)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StartVpnButton(onIntent, state.isRunning)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RestartButton(onIntent)
                TestConnectionButton(onIntent)
            }
            Text(
                text = "Успешно: Соединение заняло 60 ms",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewVpnScreen() {
    VpnScreenContent(state = VpnScreenState(), onQrCodeClick = {}, onIntent = {})
}
package com.pet.vpn_client.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.intent.VpnScreenIntent
import com.pet.vpn_client.presentation.state.VpnScreenState
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel
import com.pet.vpn_client.ui.composable_elements.ConfigDropDownMenu
import com.pet.vpn_client.ui.composable_elements.ConnectionButton
import com.pet.vpn_client.ui.composable_elements.StartVpnButton
import com.pet.vpn_client.ui.composable_elements.SubscriptionsList
import com.pet.vpn_client.ui.composable_elements.SwitchVpnProxy

//Изучить библиотеку для создания моковых объектов

@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    onQrCodeClick: () -> Unit
) {
    val viewModel: VpnScreenViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
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
            SwitchVpnProxy(onIntent)
            ConfigDropDownMenu(onIntent, onQrCodeClick)
        }

        Column(modifier = Modifier.weight(1f)) {
            SubscriptionsList(state.serverItemList)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StartVpnButton(onIntent)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionButton("R", onIntent)
                ConnectionButton("T", onIntent)
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
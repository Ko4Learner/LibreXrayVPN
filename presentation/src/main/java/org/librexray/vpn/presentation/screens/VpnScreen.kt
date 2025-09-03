package org.librexray.vpn.presentation.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.state.VpnScreenState
import org.librexray.vpn.presentation.view_model.VpnScreenViewModel
import org.librexray.vpn.presentation.composable_elements.ConfigDropDownMenu
import org.librexray.vpn.presentation.composable_elements.RestartButton
import org.librexray.vpn.presentation.composable_elements.TestConnectionButton
import org.librexray.vpn.presentation.composable_elements.ConnectToggle
import org.librexray.vpn.presentation.composable_elements.SubscriptionsList

@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onQrCodeClick: () -> Unit,
    viewModel: VpnScreenViewModel = hiltViewModel(),
    getString: (Int) -> String
) {
    val state by viewModel.state.collectAsState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val qrCodeImported =
        savedStateHandle?.getStateFlow("qrCodeImported", false)?.collectAsState()

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
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LibreXrayVpn", style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
            ConfigDropDownMenu(onIntent, onQrCodeClick)
        }

        Column(modifier = Modifier.weight(1f)) {
            SubscriptionsList(onIntent, state.serverItemList)
        }
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectToggle(onIntent, state.isRunning)
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
                text = if (state.delay != null) "Delay: ${state.delay} ms" else "ERROR",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewVpnScreen() {
    VpnScreenContent(state = VpnScreenState(), onQrCodeClick = {}, onIntent = {})
}
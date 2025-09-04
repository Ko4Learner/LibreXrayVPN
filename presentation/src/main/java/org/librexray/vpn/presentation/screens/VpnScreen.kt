package org.librexray.vpn.presentation.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // ЦЕНТР — всегда в геометрическом центре экрана
        MiddleSection(
            isRunning = state.isRunning,
            onIntent = onIntent,
            modifier = Modifier.fillMaxSize() // критично: центр сам занимает весь слой
        )

        // ВЕРХ — как есть, просто выровнен кверху
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            TopSection(
                onIntent = onIntent,
                onQrCodeClick = onQrCodeClick,
                state = state
            )
        }

        // НИЗ — как есть, выровнен книзу
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(8.dp)
        ) {
            BottomSection(
                visible = state.isRunning,
                delayMs = state.delay,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun TopSection(
    onIntent: (VpnScreenIntent) -> Unit,
    onQrCodeClick: () -> Unit,
    state: VpnScreenState
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LibreXrayVpn",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
            ConfigDropDownMenu(onIntent, onQrCodeClick)
        }

        AnimatedVisibility(visible = !state.serverItemList.isEmpty()) {
            SubscriptionsList(onIntent, state.serverItemList)
        }
    }
}

@Composable
private fun MiddleSection(
    isRunning: Boolean,
    onIntent: (VpnScreenIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ConnectToggle(onIntent = onIntent, isRunning = isRunning)
    }
}

@Composable
private fun BottomSection(
    visible: Boolean,
    delayMs: Long?,
    onIntent: (VpnScreenIntent) -> Unit
) {
    AnimatedVisibility(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RestartButton(onIntent)
                TestConnectionButton(onIntent)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = delayMs?.let { "Delay: $it ms" } ?: "—",
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
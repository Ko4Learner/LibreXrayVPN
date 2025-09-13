package org.librexray.vpn.presentation.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.librexray.vpn.domain.models.ConnectionSpeed
import org.librexray.vpn.presentation.composable_elements.ContentBottomSheet
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.state.VpnScreenState
import org.librexray.vpn.presentation.view_model.VpnScreenViewModel
import org.librexray.vpn.presentation.composable_elements.ConnectToggle
import org.librexray.vpn.presentation.composable_elements.ConnectionSpeedInfo
import org.librexray.vpn.presentation.composable_elements.ConnectionTestButton
import org.librexray.vpn.presentation.composable_elements.SubscriptionItem
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.theme.Grey80
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.presentation.models.ServerItemModel

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

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    LaunchedEffect(qrCodeImported?.value) {
        if (qrCodeImported?.value == true) {
            viewModel.onIntent(VpnScreenIntent.RefreshItemList)
            savedStateHandle.remove<Boolean>("qrCodeImported")
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetElevation = 16.dp,
        scrimColor = Grey80.copy(alpha = 0.8f),
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetContent = {
            ContentBottomSheet(
                onIntent = viewModel::onIntent,
                onQrCodeClick = onQrCodeClick,
                itemList = state.serverItemList,
                selectedServerId = state.selectedServerId,
                hideBottomSheet = {
                    scope.launch { sheetState.hide() }
                }
            )
        }
    ) {
        VpnScreenContent(
            modifier = modifier,
            state = state,
            onIntent = viewModel::onIntent,
            showBottomSheet = {
                scope.launch { sheetState.show() }
            }
        )
    }
}

@Composable
fun VpnScreenContent(
    modifier: Modifier = Modifier,
    state: VpnScreenState,
    onIntent: (VpnScreenIntent) -> Unit,
    showBottomSheet: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding()
            .background(MaterialTheme.colors.background)
    ) {
        TopSection(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .statusBarsPadding(),
            state = state,
            showBottomSheet = showBottomSheet
        )
        MiddleSection(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            isRunning = state.isRunning,
            onIntent = onIntent,
            state = state,
            showBottomSheet = showBottomSheet
        )
        BottomSection(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            visible = state.isRunning,
            delayMs = state.delay,
            connectionSpeed = state.connectionSpeed,
            onIntent = onIntent
        )
    }
}

@Composable
private fun TopSection(
    modifier: Modifier = Modifier,
    state: VpnScreenState,
    showBottomSheet: () -> Unit,
) {
    val selectedServer = remember(state.serverItemList, state.selectedServerId) {
        state.serverItemList.firstOrNull { it.guid == state.selectedServerId }
    }

    Box(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LibreXrayVpn",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onBackground
                )
                Box {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = AppIcons.Menu,
                            contentDescription = "Добавить конфигурацию",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(visible = selectedServer != null) {
                selectedServer?.let {
                    SubscriptionItem(
                        item = it,
                        selectedServerId = it.guid,
                        buttonIcon = AppIcons.arrowForward,
                        onCardClick = { _ -> showBottomSheet() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiddleSection(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    onIntent: (VpnScreenIntent) -> Unit,
    showBottomSheet: () -> Unit,
    state: VpnScreenState
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ConnectToggle(
            onIntent = onIntent,
            isRunning = isRunning,
            emptyServerList = state.serverItemList.isEmpty(),
            showBottomSheet = showBottomSheet
        )
    }
}

@Composable
private fun BottomSection(
    modifier: Modifier = Modifier,
    visible: Boolean,
    delayMs: Long?,
    connectionSpeed: ConnectionSpeed?,
    onIntent: (VpnScreenIntent) -> Unit
) {
    Box(modifier = modifier) {
        AnimatedVisibility(visible = visible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConnectionSpeedInfo(connectionSpeed = connectionSpeed)
                ConnectionTestButton(onIntent = onIntent, delayMs = delayMs)
            }
        }
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewVpnScreen() {
    LibreXrayVPNTheme {
        VpnScreenContent(
            modifier = Modifier,
            state = VpnScreenState(
                isRunning = true, serverItemList = listOf(
                    ServerItemModel(
                        guid = "1",
                        name = "My vless server config",
                        ip = "192.168.252.1",
                        protocol = "Vless"
                    )
                ), selectedServerId = "1"
            ),
            onIntent = {},
            showBottomSheet = {})
    }
}
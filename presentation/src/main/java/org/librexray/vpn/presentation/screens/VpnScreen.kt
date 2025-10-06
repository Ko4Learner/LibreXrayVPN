package org.librexray.vpn.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.domain.models.ConnectionSpeed
import org.librexray.vpn.presentation.composable_element.ContentBottomSheet
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.state.VpnScreenState
import org.librexray.vpn.presentation.view_model.VpnScreenViewModel
import org.librexray.vpn.presentation.composable_element.ConnectToggle
import org.librexray.vpn.presentation.composable_element.ConnectionSpeedInfo
import org.librexray.vpn.presentation.composable_element.ConnectionTestButton
import org.librexray.vpn.presentation.composable_element.item.SubscriptionItem
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.design_system.theme.Grey80
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.presentation.model.ServerItemModel
import org.librexray.vpn.presentation.state.VpnScreenError

@Composable
fun VpnScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onQrCodeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: VpnScreenViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val qrCodeImported =
        savedStateHandle?.getStateFlow("qrCodeImported", false)?.collectAsState()
    LaunchedEffect(qrCodeImported?.value) {
        if (qrCodeImported?.value == true) {
            viewModel.onIntent(VpnScreenIntent.RefreshItemList)
            savedStateHandle.remove<Boolean>("qrCodeImported")
        }
    }

    LaunchedEffect(state.error) {
        when (state.error) {
            VpnScreenError.UpdateServerListError -> {
                val res = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.update_server_list_error),
                    actionLabel = context.getString(R.string.repeat),
                    duration = SnackbarDuration.Short
                )
                if (res == SnackbarResult.ActionPerformed) {
                    viewModel.onIntent(VpnScreenIntent.RefreshItemList)
                }
                viewModel.onIntent(VpnScreenIntent.ConsumeError)
            }

            null -> Unit
            else -> {
                errorHandler(state.error, context)
                viewModel.onIntent(VpnScreenIntent.ConsumeError)
            }
        }
    }

    VpnScreenContent(
        modifier = modifier,
        state = state,
        onSettingsClick = onSettingsClick,
        onIntent = viewModel::onIntent,
        onQrCodeClick = onQrCodeClick
    )
}

@Composable
private fun VpnScreenContent(
    modifier: Modifier = Modifier,
    state: VpnScreenState,
    onSettingsClick: () -> Unit,
    onIntent: (VpnScreenIntent) -> Unit,
    onQrCodeClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetElevation = 16.dp,
        scrimColor = Grey80.copy(alpha = 0.8f),
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetContent = {
            ContentBottomSheet(
                onIntent = onIntent,
                onQrCodeClick = onQrCodeClick,
                itemList = state.serverItemList,
                selectedServerId = state.selectedServerId,
                hideBottomSheet = {
                    scope.launch { sheetState.hide() }
                }
            )
        }
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
                serverItemList = state.serverItemList,
                selectedServerId = state.selectedServerId,
                onSettingsClick = onSettingsClick,
                showBottomSheet = { scope.launch { sheetState.show() } }
            )
            MiddleSection(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                isRunning = state.isRunning,
                isLoading = state.isLoading,
                wasNotificationPermissionAsked = state.wasNotificationPermissionAsked,
                serverListIsEmpty = state.serverItemList.isEmpty(),
                onIntent = onIntent,
                showBottomSheet = { scope.launch { sheetState.show() } }
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
}

@Composable
private fun TopSection(
    modifier: Modifier = Modifier,
    serverItemList: List<ServerItemModel>,
    selectedServerId: String?,
    onSettingsClick: () -> Unit,
    showBottomSheet: () -> Unit,
) {
    val selectedServer = remember(serverItemList, selectedServerId) {
        serverItemList.firstOrNull { it.guid == selectedServerId }
    }

    Box(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = AppIcons.AppIcon.rememberPainter(),
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onBackground
                    )
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = AppIcons.Menu.rememberPainter(),
                        contentDescription = stringResource(R.string.settings),
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedServer != null,
                enter = expandVertically(
                    expandFrom = Alignment.Top
                ) + fadeIn(),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top
                ) + fadeOut()
            ) {
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
    isLoading: Boolean,
    wasNotificationPermissionAsked: Boolean,
    serverListIsEmpty :Boolean,
    onIntent: (VpnScreenIntent) -> Unit,
    showBottomSheet: () -> Unit
) {

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (!isLoading) {
            ConnectToggle(
                onIntent = onIntent,
                isRunning = isRunning,
                emptyServerList = serverListIsEmpty,
                showBottomSheet = showBottomSheet,
                wasNotificationPermissionAsked = wasNotificationPermissionAsked
            )
        } else {
            Box(Modifier.size(160.dp))
        }
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
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(
                expandFrom = Alignment.Bottom
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Bottom
            ) + fadeOut()
        ) {
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

private fun errorHandler(
    error: VpnScreenError?,
    context: Context
) {
    when (error) {
        VpnScreenError.DeleteConfigError -> {
            Toast.makeText(
                context,
                context.getString(R.string.delete_configuration_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        VpnScreenError.EmptyConfigError -> {
            Toast.makeText(
                context,
                context.getString(R.string.configuration_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }

        VpnScreenError.ImportConfigError -> {
            Toast.makeText(
                context,
                context.getString(R.string.configuration_import_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        VpnScreenError.StartError -> {
            Toast.makeText(
                context,
                context.getString(R.string.start_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        VpnScreenError.StopError -> {
            Toast.makeText(
                context,
                context.getString(R.string.stop_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        VpnScreenError.TestConnectionError -> {
            Toast.makeText(
                context,
                context.getString(R.string.test_connection_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        VpnScreenError.SelectServerError -> {
            Toast.makeText(
                context,
                context.getString(R.string.server_selection_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        else -> Unit
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewVpnScreen() {
    LibreXrayVPNTheme {
        VpnScreenContent(
            modifier = Modifier,
            state = VpnScreenState(
                isLoading = false,
                isRunning = true,
                serverItemList = listOf(
                    ServerItemModel(
                        guid = "1",
                        name = "My vless server config",
                        ip = "192.168.252.1",
                        protocol = "Vless"
                    )
                ),
                selectedServerId = "1"
            ),
            onSettingsClick = {},
            onIntent = {},
            onQrCodeClick = {})
    }
}
package org.librexray.vpn.presentation.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.librexray.vpn.presentation.composable_elements.items.SettingItem
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.presentation.intent.SettingsScreenIntent
import org.librexray.vpn.presentation.state.SettingsScreenState
import org.librexray.vpn.presentation.view_model.SettingsScreenViewModel
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.librexray.vpn.domain.models.ThemeMode
import org.librexray.vpn.presentation.design_system.theme.Grey80

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    getString: (Int) -> String,
) {
    val state by viewModel.state.collectAsState()

    SettingsScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        getString = getString
    )
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsScreenState,
    onIntent: (SettingsScreenIntent) -> Unit,
    onBackClick: () -> Unit,
    getString: (Int) -> String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    var currentSheet: SettingsSheet? by remember { mutableStateOf(null) }
    fun openBottomSheet(sheet: SettingsSheet) {
        currentSheet = sheet
        scope.launch { sheetState.show() }
    }

    fun closeBottomSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { currentSheet = null }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetElevation = 16.dp,
        scrimColor = Grey80.copy(alpha = 0.8f),
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetContent = {
            when (currentSheet) {
                SettingsSheet.About -> TODO()
                SettingsSheet.Language -> TODO()
                SettingsSheet.Theme ->
                    ThemeBottomSheet(
                        state = state,
                        onIntent = onIntent,
                        getString = getString,
                        closeBottomSheet = { closeBottomSheet() }
                    )

                null -> closeBottomSheet()
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = onBackClick
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = AppIcons.arrowBack.rememberPainter(),
                            contentDescription = "Назад",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Настройки",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onBackground
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                SettingItem(
                    title = "Тема приложения",
                    icon = AppIcons.Theme,
                    onClick = { openBottomSheet(SettingsSheet.Theme) })
                SettingItem(
                    title = "Язык приложения",
                    icon = AppIcons.Language,
                    onClick = { openBottomSheet(SettingsSheet.Language) })
                SettingItem(
                    title = "Github",
                    icon = AppIcons.Github,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/Ko4Learner/LibreXrayVPN".toUri()
                            )
                        )

                    })
                SettingItem(
                    title = "О приложении",
                    icon = AppIcons.Info,
                    onClick = { openBottomSheet(SettingsSheet.About) })
            }
        }
    }
}

@Composable
fun ThemeBottomSheet(
    modifier: Modifier = Modifier,
    state: SettingsScreenState,
    onIntent: (SettingsScreenIntent) -> Unit,
    getString: (Int) -> String,
    closeBottomSheet: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colors.surface
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Тема приложения",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
            IconButton(onClick = closeBottomSheet) {
                Icon(
                    painter = AppIcons.Close.rememberPainter(),
                    contentDescription = "Close",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
        ThemeMode.entries.forEach { mode ->
            val isSelected = state.themeMode == mode
            val cardModifier = Modifier
                .fillMaxWidth().let {
                if (isSelected) it.border(
                    color = MaterialTheme.colors.primary,
                    width = 1.dp,
                    shape = RoundedCornerShape(16.dp)
                ) else it
            }
            Card(
                modifier = cardModifier,
                shape = RoundedCornerShape(16.dp),
                elevation = 0.dp,
                backgroundColor = if (isSelected) MaterialTheme.colors.surface
                else MaterialTheme.colors.surface.copy(
                    alpha = 0.7f
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .clickable { onIntent(SettingsScreenIntent.SetTheme(mode)) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = when (mode) {
                            ThemeMode.SYSTEM -> "Системная тема"
                            ThemeMode.LIGHT -> "Светлая тема"
                            ThemeMode.DARK -> "Тёмная тема"
                        },
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onBackground
                    )
                    RadioButton(
                        modifier = Modifier,
                        selected = isSelected,
                        onClick = { onIntent(SettingsScreenIntent.SetTheme(mode)) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary),
                    )
                }
            }
        }
    }
}

private sealed class SettingsSheet {
    data object Theme : SettingsSheet()
    data object Language : SettingsSheet()
    data object About : SettingsSheet()
}

@Preview
@Composable
fun SettingsScreenContentPreview() {
    LibreXrayVPNTheme {
        SettingsScreenContent(
            state = SettingsScreenState(),
            onIntent = {},
            onBackClick = {},
            getString = { "" })
    }
}
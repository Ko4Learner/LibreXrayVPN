package org.librexray.vpn.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.librexray.vpn.presentation.composable_elements.SettingItem
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.presentation.intent.SettingsScreenIntent
import org.librexray.vpn.presentation.state.SettingsScreenState
import org.librexray.vpn.presentation.view_model.SettingsScreenViewModel

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
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsScreenState,
    onIntent: (SettingsScreenIntent) -> Unit,
    onBackClick: () -> Unit,
    getString: (Int) -> String,
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
            SettingItem(title = "Тема приложения", icon = AppIcons.Theme)
            SettingItem(title = "Язык приложения", icon = AppIcons.Language)
            SettingItem(title = "Github", icon = AppIcons.Github)
            SettingItem(title = "О приложении", icon = AppIcons.Info)
        }


    }
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
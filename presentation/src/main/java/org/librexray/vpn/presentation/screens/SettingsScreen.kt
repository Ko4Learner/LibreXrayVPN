package org.librexray.vpn.presentation.screens

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.librexray.vpn.presentation.composable_element.item.SettingItem
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.presentation.intent.SettingsScreenIntent
import org.librexray.vpn.presentation.state.SettingsScreenState
import org.librexray.vpn.presentation.view_model.SettingsScreenViewModel
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.coreandroid.utils.Constants
import org.librexray.vpn.domain.models.AppLocale
import org.librexray.vpn.domain.models.ThemeMode
import org.librexray.vpn.presentation.design_system.theme.Grey80

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    SettingsScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick
    )
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsScreenState,
    onIntent: (SettingsScreenIntent) -> Unit,
    onBackClick: () -> Unit
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
                SettingsSheet.Language -> LanguageBottomSheet(
                    localeMode = state.localeMode,
                    onIntent = onIntent,
                    closeBottomSheet = { closeBottomSheet() }
                )

                SettingsSheet.Theme ->
                    ThemeBottomSheet(
                        themeMode = state.themeMode,
                        onIntent = onIntent,
                        closeBottomSheet = { closeBottomSheet() }
                    )

                SettingsSheet.About -> AboutBottomSheet(
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
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onBackground
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                SettingItem(
                    title = stringResource(R.string.language),
                    icon = AppIcons.Language,
                    onClick = { openBottomSheet(SettingsSheet.Language) })
                SettingItem(
                    title = stringResource(R.string.application_theme),
                    icon = AppIcons.Theme,
                    onClick = { openBottomSheet(SettingsSheet.Theme) })
                SettingItem(
                    title = stringResource(R.string.github),
                    icon = AppIcons.Github,
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Constants.GITHUB_URI.toUri()
                            )
                        )

                    })
                SettingItem(
                    title = stringResource(R.string.about_app),
                    icon = AppIcons.Info,
                    onClick = { openBottomSheet(SettingsSheet.About) })
            }
        }
    }
}

@Composable
private fun LanguageBottomSheet(
    modifier: Modifier = Modifier,
    localeMode: AppLocale,
    onIntent: (SettingsScreenIntent) -> Unit,
    closeBottomSheet: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
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
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
            IconButton(onClick = closeBottomSheet) {
                Icon(
                    painter = AppIcons.Close.rememberPainter(),
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
        AppLocale.entries.forEach { mode ->
            val isSelected = localeMode == mode
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
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(SettingsScreenIntent.SetLocale(mode)) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        text = when (mode) {
                            AppLocale.SYSTEM -> stringResource(R.string.system_language)
                            AppLocale.EN -> stringResource(R.string.english_language)
                            AppLocale.RU -> stringResource(R.string.russian_language)
                        },
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onBackground
                    )
                    RadioButton(
                        modifier = Modifier,
                        selected = isSelected,
                        onClick = { onIntent(SettingsScreenIntent.SetLocale(mode)) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeBottomSheet(
    modifier: Modifier = Modifier,
    themeMode: ThemeMode,
    onIntent: (SettingsScreenIntent) -> Unit,
    closeBottomSheet: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
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
                text = stringResource(R.string.application_theme),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
            IconButton(onClick = closeBottomSheet) {
                Icon(
                    painter = AppIcons.Close.rememberPainter(),
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
        ThemeMode.entries.forEach { mode ->
            val isSelected = themeMode == mode
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
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(SettingsScreenIntent.SetTheme(mode)) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        text = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.system_theme)
                            ThemeMode.LIGHT -> stringResource(R.string.light_theme)
                            ThemeMode.DARK -> stringResource(R.string.dark_theme)
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

@Composable
private fun AboutBottomSheet(
    modifier: Modifier = Modifier,
    closeBottomSheet: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
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
                text = stringResource(R.string.about_app),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
            IconButton(onClick = closeBottomSheet) {
                Icon(
                    painter = AppIcons.Close.rememberPainter(),
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.background
        ) {
            Text(
                text = stringResource(R.string.about_text, Constants.VERSION_CODE),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.85f)
            )
        }
    }
}

private sealed class SettingsSheet {
    data object Theme : SettingsSheet()
    data object Language : SettingsSheet()
    data object About : SettingsSheet()
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenContentPreview() {
    LibreXrayVPNTheme {
        SettingsScreenContent(
            state = SettingsScreenState(),
            onIntent = {},
            onBackClick = {})
    }
}
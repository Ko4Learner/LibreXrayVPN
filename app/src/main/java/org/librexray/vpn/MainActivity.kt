package org.librexray.vpn

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import org.librexray.vpn.presentation.navigation.Navigation
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.coreandroid.utils.LocaleHelper
import org.librexray.vpn.presentation.view_model.SettingsScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - root activity for the VPN client UI.
 *
 * Responsibilities:
 * - Hosts the Jetpack Compose content.
 * - Applies dynamic locale updates based on SettingsScreenViewModel state.
 * - Sets up the root navigation controller and app-wide Scaffold layout.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SettingsScreenViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current
            val localizedContext = remember(state.locale) {
                LocaleHelper.updateLocale(context, state.locale)
            }
            LibreXrayVPNTheme(themeMode = state.themeMode) {
                val view = LocalView.current
                val window = (view.context as Activity).window
                val barColor = MaterialTheme.colors.background
                val isLight = barColor.luminance() > 0.5f

                SideEffect {
                    WindowInsetsControllerCompat(window, view).apply {
                        isAppearanceLightStatusBars = isLight
                        isAppearanceLightNavigationBars = isLight
                    }
                    @Suppress("DEPRECATION")
                    window.statusBarColor = barColor.toArgb()
                }


                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .systemBarsPadding()
                        .fillMaxSize()
                ) { innerPadding ->
                    Navigation(
                        navController = navController, innerPadding = innerPadding,
                        // Pass a localized string getter to navigation graph.
                        getString = { resId: Int ->
                            localizedContext.getString(resId)
                        })
                }
            }
        }
    }
}
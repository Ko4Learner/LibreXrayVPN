package org.librexray.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import org.librexray.vpn.presentation.navigation.Navigation
import org.librexray.vpn.presentation.theme.LibreXrayVPNTheme
import org.librexray.vpn.core.utils.LocaleHelper
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
        // Enable full content drawing under system bars (Material 3 edge-to-edge).
        enableEdgeToEdge()
        setContent {
            val viewModel: SettingsScreenViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current
            val localizedContext = remember(state.locale) {
                LocaleHelper.updateLocale(context, state.locale)
            }
            LibreXrayVPNTheme {
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
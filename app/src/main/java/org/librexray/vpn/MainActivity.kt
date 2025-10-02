package org.librexray.vpn

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import org.librexray.vpn.presentation.navigation.Navigation
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme
import org.librexray.vpn.presentation.view_model.SettingsScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.librexray.vpn.domain.interfaces.interactor.SettingsInteractor
import javax.inject.Inject

/**
 * MainActivity - root activity for the VPN client UI.
 *
 * Responsibilities:
 * - Hosts the Jetpack Compose content.
 * - Applies dynamic locale updates based on SettingsScreenViewModel state.
 * - Sets up the root navigation controller and app-wide Scaffold layout.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settingsInteractor: SettingsInteractor
    private val settingsViewModel: SettingsScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepOnScreen = true
        splash.setKeepOnScreenCondition { keepOnScreen }

        val storedLocaleTag = runBlocking {
            settingsInteractor.observeLocale().first().toTag()
        }
        val currentLocaleTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentLocaleTag != storedLocaleTag) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(storedLocaleTag)
            )
            return
        }
        keepOnScreen = false

        enableEdgeToEdge()
        setContent {
            val viewModel: SettingsScreenViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

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
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier
                        .systemBarsPadding()
                        .fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Navigation(
                        navController = navController,
                        innerPadding = innerPadding,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.state
                    .map { it.localeMode.toTag() }
                    .drop(1)
                    .distinctUntilChanged()
                    .collect { newTag ->
                        val now = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                        if (now != newTag) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(newTag)
                            )
                        }
                    }
            }
        }
    }
}
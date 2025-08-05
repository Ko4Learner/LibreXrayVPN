package com.pet.vpn_client

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
import com.pet.vpn_client.presentation.navigation.Navigation
import com.pet.vpn_client.core.ui.theme.VPN_ClientTheme
import com.pet.vpn_client.core.utils.LocaleHelper
import com.pet.vpn_client.presentation.view_model.SettingsScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

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
            VPN_ClientTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.Companion
                        .systemBarsPadding()
                        .fillMaxSize()
                ) { innerPadding ->
                    Navigation(
                        navController = navController, innerPadding = innerPadding,
                        getString = { resId: Int ->
                            localizedContext.getString(resId)
                        })
                }
            }
        }
    }
}
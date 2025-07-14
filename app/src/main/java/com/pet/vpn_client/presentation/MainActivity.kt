package com.pet.vpn_client.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pet.vpn_client.presentation.navigation.Navigation
import com.pet.vpn_client.ui.theme.VPN_ClientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VPN_ClientTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.Companion
                        .systemBarsPadding()
                        .fillMaxSize()
                ) { innerPadding ->
                    Navigation(navController = navController, innerPadding = innerPadding)
                }
            }
        }
    }
}
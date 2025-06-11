package com.pet.vpn_client.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel
import com.pet.vpn_client.ui.composable_elements.ConfigDropDownMenu
import com.pet.vpn_client.ui.composable_elements.ConnectionButton
import com.pet.vpn_client.ui.composable_elements.StartVpnButton
import com.pet.vpn_client.ui.composable_elements.SubscriptionsList
import com.pet.vpn_client.ui.composable_elements.SwitchVpnProxy

//Изучить библиотеку для создания моковых объектов

//Принято использовать отдельные Preview функций
@Preview(
    name = "VPNScreen Preview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun VpnScreen(modifier: Modifier = Modifier, viewModel: VpnScreenViewModel = hiltViewModel()) {
    val viewModel: VpnScreenViewModel = hiltViewModel()
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
            .background(colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitchVpnProxy(viewModel)
            ConfigDropDownMenu(viewModel)
        }

        Column(modifier = Modifier.weight(1f)) {
            SubscriptionsList(listOf())
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StartVpnButton(viewModel)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionButton("R", viewModel)
                ConnectionButton("T", viewModel)
            }
            Text(
                text = "Успешно: Соединение заняло 60 ms",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
package com.pet.vpn_client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Switch
import com.pet.vpn_client.R
import com.pet.vpn_client.presentation.view_model.VpnScreenViewModel
import com.pet.vpn_client.ui.composable_elements.MenuItem
import com.pet.vpn_client.ui.composable_elements.SubscriptionItem
import com.pet.vpn_client.ui.models.SubscriptionItemModel

//Принято использовать отдельный Preview функций
@Preview(
    name = "VPNScreen Preview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun VpnScreen(modifier: Modifier = Modifier) {
    val viewModel: VpnScreenViewModel = hiltViewModel()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Row(
            modifier = modifier
                .padding(horizontal = 8.dp)
                .height(46.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitchVpnProxy(viewModel)
            MenuItem()
        }

        SubscriptionsList(listOf())
        StartVpnButton(viewModel)
    }
}

@Composable
fun StartVpnButton(viewModel: VpnScreenViewModel) {
    Box(
        modifier = Modifier
            .background(color = colorScheme.secondary, shape = CircleShape)
            .size(200.dp)
            .clickable {
                viewModel.toggleVpnProxy()
            },
        contentAlignment = Alignment.Center

    ) {
        //TODO Add VPN icon
    }
}

@Composable
fun SwitchVpnProxy(viewModel: VpnScreenViewModel) {

    var isChecked by remember {
        mutableStateOf(false)
    }
    Switch(
        checked = isChecked,
        onCheckedChange = {
            isChecked = it
            viewModel.switchVpnProxy()
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = colorScheme.primary,
            uncheckedThumbColor = colorScheme.secondary,
            checkedTrackColor = Color.Green,
            uncheckedTrackColor = Color.White
        )
    )
}

@Composable
fun SubscriptionsList(itemList: List<SubscriptionItemModel>) {
    LazyColumn(
        modifier = Modifier
            .background(Color.Gray)
    ) {
        itemsIndexed(
            items = listOf(
                SubscriptionItemModel(
                    imageCountryId = R.drawable.flag_russia,
                    name = "Subscription 1",
                    ip = "192.168.1.1",
                    protocol = "Vless",
                    description = "Description 1"

                )
            )
        )
        { _, item ->
            SubscriptionItem(item = item)
        }
    }
}
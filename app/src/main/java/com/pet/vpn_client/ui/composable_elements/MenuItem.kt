package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.pet.vpn_client.R

@Composable
fun MenuItem(){
    Image(
        painter = painterResource(id = R.drawable.baseline_view_stream_24),
        contentDescription = "Settings"
    )
}
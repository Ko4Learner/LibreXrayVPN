package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pet.vpn_client.ui.models.SubscriptionItemModel

@Composable
fun SubscriptionItem(item: SubscriptionItemModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(color = MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(item.imageCountryId), contentDescription = "countryFlag",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(text = item.ip, style = MaterialTheme.typography.titleMedium)
                Text(text = item.protocol, style = MaterialTheme.typography.titleMedium)
                Text(text = item.description, style = MaterialTheme.typography.titleMedium)

            }

        }
    }
}
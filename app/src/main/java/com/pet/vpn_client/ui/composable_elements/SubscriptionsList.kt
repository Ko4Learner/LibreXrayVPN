package com.pet.vpn_client.ui.composable_elements

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.pet.vpn_client.R
import com.pet.vpn_client.ui.models.SubscriptionItemModel

@Composable
fun SubscriptionsList(itemList: List<SubscriptionItemModel>) {
    LazyColumn {
        itemsIndexed(
            items = listOf(
                SubscriptionItemModel(
                    imageCountryId = R.drawable.flag_russia,
                    name = "Subscription 1",
                    ip = "192.168.1.1",
                    protocol = "Vless",
                    description = "Description 1"

                ),
                SubscriptionItemModel(
                    imageCountryId = R.drawable.flag_russia,
                    name = "Subscription 2",
                    ip = "192.168.1.2",
                    protocol = "Vless",
                    description = "Description 2"

                ),
                SubscriptionItemModel(
                    imageCountryId = R.drawable.flag_russia,
                    name = "Subscription 3",
                    ip = "192.168.1.3",
                    protocol = "Vless",
                    description = "Description 3"

                ),
                SubscriptionItemModel(
                    imageCountryId = R.drawable.flag_russia,
                    name = "Subscription 4",
                    ip = "192.168.1.4",
                    protocol = "Vless",
                    description = "Description 4"

                ),
                SubscriptionItemModel(
                    imageCountryId = R.drawable.flag_russia,
                    name = "Subscription 5",
                    ip = "192.168.1.5",
                    protocol = "Vless",
                    description = "Description 5"

                ),
            )
        )
        { _, item ->
            SubscriptionItem(item = item)
        }
    }
}
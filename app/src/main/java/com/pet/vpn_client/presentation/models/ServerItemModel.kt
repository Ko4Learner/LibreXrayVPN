package com.pet.vpn_client.presentation.models

import com.pet.vpn_client.domain.models.ConfigProfileItem

data class ServerItemModel(
    val guid: String,
    //TODO отдельная модель для ui
    val profile: ConfigProfileItem
)


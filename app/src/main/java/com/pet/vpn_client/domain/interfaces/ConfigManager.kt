package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.data.dto.XrayConfig.OutboundBean
import com.pet.vpn_client.data.dto.XrayConfig.OutboundBean.StreamSettingsBean
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType

interface ConfigManager {
    fun getCoreConfig(guid: String): ConfigResult
    fun createInitOutbound(configType: EConfigType): OutboundBean?
    fun populateTransportSettings(
        streamSettings: StreamSettingsBean,
        profileItem: ConfigProfileItem
    ): String?
    fun populateTlsSettings(
        streamSettings: StreamSettingsBean,
        profileItem: ConfigProfileItem,
        sniExt: String?
    )
}
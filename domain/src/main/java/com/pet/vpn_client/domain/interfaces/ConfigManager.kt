package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.XrayConfig
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean.StreamSettingsBean
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType

interface ConfigManager {
    fun getCoreConfig(guid: String): ConfigResult
    fun createInitOutbound(configType: EConfigType): XrayConfig.OutboundBean?
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
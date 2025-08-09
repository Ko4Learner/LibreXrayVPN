package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.XrayConfig

interface CoreConfigProvider {
    fun getCoreConfig(guid: String): ConfigResult
    fun createInitOutbound(configType: EConfigType): XrayConfig.OutboundBean?
    fun populateTransportSettings(
        streamSettings: XrayConfig.OutboundBean.StreamSettingsBean,
        profileItem: ConfigProfileItem
    ): String?

    fun populateTlsSettings(
        streamSettings: XrayConfig.OutboundBean.StreamSettingsBean,
        profileItem: ConfigProfileItem,
        sniExt: String?
    )
}
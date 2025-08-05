package com.pet.vpn_client.domain.interfaces.repository

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.XrayConfig

interface ConfigRepository {
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
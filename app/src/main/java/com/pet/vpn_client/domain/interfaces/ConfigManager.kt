package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.ConfigResult

interface ConfigManager {
    fun getCoreConfig(guid: String): ConfigResult
}
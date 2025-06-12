package com.pet.vpn_client.domain.interfaces.interactor

import com.pet.vpn_client.domain.models.ConfigProfileItem

interface ConfigInteractor {
    suspend fun importClipboardConfig(): Int
    suspend fun getServerList(): List<String>
    suspend fun getServerConfig(guid: String): ConfigProfileItem?
}
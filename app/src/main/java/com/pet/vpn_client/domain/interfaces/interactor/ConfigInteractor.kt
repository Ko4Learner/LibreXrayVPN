package com.pet.vpn_client.domain.interfaces.interactor

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.FrameData

interface ConfigInteractor {
    suspend fun importClipboardConfig(): Int
    suspend fun importQrCodeConfig(frameData: FrameData): Int
    suspend fun getServerList(): List<String>
    suspend fun getServerConfig(guid: String): ConfigProfileItem?
    suspend fun deleteItem(id: String)
}
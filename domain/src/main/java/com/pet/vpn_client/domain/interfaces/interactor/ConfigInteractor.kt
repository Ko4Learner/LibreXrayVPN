package com.pet.vpn_client.domain.interfaces.interactor

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.FrameData
import com.pet.vpn_client.domain.models.ImportResult

interface ConfigInteractor {
    suspend fun importClipboardConfig(): ImportResult
    suspend fun importQrCodeConfig(frameData: FrameData): ImportResult
    suspend fun getServerList(): List<String>
    suspend fun getServerConfig(guid: String): ConfigProfileItem?
    suspend fun deleteItem(guid: String)
    suspend fun getSelectedServer(): String?
}
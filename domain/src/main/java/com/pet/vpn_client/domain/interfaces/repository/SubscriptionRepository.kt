package com.pet.vpn_client.domain.interfaces.repository

import com.pet.vpn_client.domain.models.FrameData
import com.pet.vpn_client.domain.models.ImportResult

interface SubscriptionRepository {
    suspend fun importClipboard(): ImportResult
    suspend fun importQrCode(frameData: FrameData): ImportResult
}
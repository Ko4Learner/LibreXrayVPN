package com.pet.vpn_client.domain.interfaces.repository

import com.pet.vpn_client.domain.models.FrameData

interface SubscriptionRepository {
    suspend fun importClipboard(): Int
    suspend fun importQrCode(frameData: FrameData): Int
}
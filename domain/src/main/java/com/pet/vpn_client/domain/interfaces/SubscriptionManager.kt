package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.FrameData

interface SubscriptionManager {
    suspend fun importClipboard(): Int
    suspend fun importQrCode(frameData: FrameData): Int
}
package com.pet.vpn_client.domain.interfaces

interface SubscriptionManager {
    suspend fun importClipboard(): Int
}
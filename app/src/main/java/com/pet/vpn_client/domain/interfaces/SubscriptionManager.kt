package com.pet.vpn_client.domain.interfaces

interface SubscriptionManager {
    suspend fun importBatchConfig(server: String?, subid: String, append: Boolean): Pair<Int, Int>
    suspend fun importClipboard() : Boolean
}
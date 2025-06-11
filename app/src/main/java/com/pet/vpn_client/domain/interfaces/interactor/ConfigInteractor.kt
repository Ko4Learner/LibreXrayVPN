package com.pet.vpn_client.domain.interfaces.interactor

interface ConfigInteractor {
    suspend fun importClipboardConfig(): Int
    //TODO нужен ли MutableList?
    suspend fun getServerList(): List<String>
}
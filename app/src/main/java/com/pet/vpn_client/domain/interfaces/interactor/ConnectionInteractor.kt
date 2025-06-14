package com.pet.vpn_client.domain.interfaces.interactor

interface ConnectionInteractor {
    suspend fun startConnection(): Boolean
    suspend fun stopConnection()
    suspend fun restartConnection(): Boolean
    suspend fun testConnection(): Boolean
}
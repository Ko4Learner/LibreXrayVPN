package com.pet.vpn_client.domain.interfaces.interactor

interface ConnectionInteractor {
    suspend fun startConnection(): Boolean
    suspend fun stopConnection()
    suspend fun testConnection(): Long?
    fun restartConnection()
}
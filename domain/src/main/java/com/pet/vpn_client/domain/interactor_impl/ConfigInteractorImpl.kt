package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.SubscriptionRepository
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.FrameData
import javax.inject.Inject

class ConfigInteractorImpl @Inject constructor(
    val subscriptionRepository: SubscriptionRepository,
    val keyValueStorage: KeyValueStorage
) : ConfigInteractor {
    override suspend fun importClipboardConfig(): Int {
        return subscriptionRepository.importClipboard()
    }

    override suspend fun importQrCodeConfig(frameData: FrameData): Int {
        return subscriptionRepository.importQrCode(frameData)
    }

    override suspend fun getServerList(): List<String> {
        return keyValueStorage.decodeServerList()
    }

    override suspend fun getServerConfig(guid: String): ConfigProfileItem? {
        return keyValueStorage.decodeServerConfig(guid)
    }

    override suspend fun deleteItem(id: String) {
        keyValueStorage.removeServer(id)
    }

    override suspend fun getSelectedServer(): String? {
        return keyValueStorage.getSelectServer()
    }
}
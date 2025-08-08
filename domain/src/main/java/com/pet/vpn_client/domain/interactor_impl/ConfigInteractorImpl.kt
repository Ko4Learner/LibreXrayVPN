package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.SubscriptionRepository
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.FrameData
import com.pet.vpn_client.domain.models.ImportResult
import javax.inject.Inject


class ConfigInteractorImpl @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val keyValueStorage: KeyValueStorage
) : ConfigInteractor {
    override suspend fun importClipboardConfig(): ImportResult {
        return subscriptionRepository.importClipboard()
    }

    override suspend fun importQrCodeConfig(frameData: FrameData): ImportResult {
        return subscriptionRepository.importQrCode(frameData)
    }

    override suspend fun getServerList(): List<String> {
        return keyValueStorage.decodeServerList()
    }

    override suspend fun getServerConfig(guid: String): ConfigProfileItem? {
        return keyValueStorage.decodeServerConfig(guid)
    }

    override suspend fun deleteItem(guid: String) {
        keyValueStorage.removeServer(guid)
    }

    override suspend fun getSelectedServer(): String? {
        return keyValueStorage.getSelectServer()
    }
}
package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.SubscriptionManager
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor
import javax.inject.Inject

class ConfigInteractorImpl @Inject constructor(
    val subscriptionManager: SubscriptionManager,
    val keyValueStorage: KeyValueStorage
) : ConfigInteractor {
    override suspend fun importClipboardConfig(): Int {
        return subscriptionManager.importClipboard()
    }

    override suspend fun getServerList(): List<String> {
        return keyValueStorage.decodeServerList()
    }
}
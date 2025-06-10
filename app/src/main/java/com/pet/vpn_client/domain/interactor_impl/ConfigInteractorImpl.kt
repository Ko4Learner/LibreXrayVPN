package com.pet.vpn_client.domain.interactor_impl

import com.pet.vpn_client.domain.interfaces.SubscriptionManager
import com.pet.vpn_client.domain.interfaces.interactor.ConfigInteractor

class ConfigInteractorImpl(val subscriptionManager: SubscriptionManager) : ConfigInteractor {
    override suspend fun importClipboardConfig() {
        subscriptionManager.importClipboard()
    }
}
package org.librexray.vpn.domain.interactor_impl

import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.interfaces.repository.ServerConfigImportRepository
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.FrameData
import org.librexray.vpn.domain.models.ImportResult
import javax.inject.Inject


class ConfigInteractorImpl @Inject constructor(
    private val serverConfigImportRepository: ServerConfigImportRepository,
    private val keyValueStorage: KeyValueStorage
) : ConfigInteractor {
    override suspend fun importClipboardConfig(): ImportResult {
        return serverConfigImportRepository.importFromClipboard()
    }

    override suspend fun importQrCodeConfig(frameData: FrameData): ImportResult {
        return serverConfigImportRepository.importFromQrFrame(frameData)
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
        return keyValueStorage.getSelectedServer()
    }

    override suspend fun setSelectedServer(serverId: String) {
        keyValueStorage.setSelectedServer(serverId)
    }
}
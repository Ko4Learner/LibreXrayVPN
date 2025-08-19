package com.pet.vpn_client.domain.interfaces.interactor

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.FrameData
import com.pet.vpn_client.domain.models.ImportResult

/**
 * Coordinates importing configurations and profile lookups.
 *
 * Contracts:
 * - importClipboardConfig(): returns ImportResult.Success | ImportResult.Empty | ImportResult.Error;
 * - importQrCodeConfig(frameData): same ImportResult tri-state contract.
 * - getServerList(): returns a list of stored ids (GUIDs); may be empty.
 * - getServerConfig(guid): returns ConfigProfileItem or null if not found.
 * - deleteItem(guid): removing a non-existing id has no effect.
 * - getSelectedServer(): returns the selected id (GUID) or null.
 */
interface ConfigInteractor {
    suspend fun importClipboardConfig(): ImportResult
    suspend fun importQrCodeConfig(frameData: FrameData): ImportResult
    suspend fun getServerList(): List<String>
    suspend fun getServerConfig(guid: String): ConfigProfileItem?
    suspend fun deleteItem(guid: String)
    suspend fun getSelectedServer(): String?
}
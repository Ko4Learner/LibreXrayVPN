package com.pet.vpn_client.domain.interfaces

import com.pet.vpn_client.domain.models.ConfigProfileItem

/**
 * Minimal key–value storage facade for VPN profiles and app settings.
 * Contracts:
 * - getSelectedServer()/setSelectedServer(guid):
 *     Gets/sets the id (GUID) of the currently selected server.
 * - encodeServerList(list)/decodeServerList():
 *     Replaces/reads the entire ordered list of stored server ids. The list represents
 *     ids only (not full profiles).
 *
 * - encodeServerConfig(guid, config)/decodeServerConfig(guid):
 *     Writes/reads a single profile by id.
 * - removeServer(guid):
 *     Deletes the profile for the id.
 *
 * - encodeSettings(key, value)/decodeSettingsString(key):
 *     Writes/reads simple string settings.
 */
interface KeyValueStorage {
    fun getSelectedServer(): String?
    fun setSelectedServer(guid: String)
    fun encodeServerList(serverList: List<String>)
    fun decodeServerList(): List<String>
    fun encodeServerConfig(guid: String, config: ConfigProfileItem): String
    fun decodeServerConfig(guid: String): ConfigProfileItem?
    fun removeServer(guid: String)
    fun encodeSettingsString(key: String, value: String?)
    fun decodeSettingsString(key: String): String?
}
package com.pet.vpn_client.domain.interfaces.repository

import com.pet.vpn_client.domain.models.FrameData
import com.pet.vpn_client.domain.models.ImportResult

/**
 * Imports configuration profiles from user-facing sources (clipboard, QR frames).
 *
 * Contracts:
 * - importClipboard():
 *     Reads textual content from the system clipboard, parses supported protocol URIs
 *     (e.g., VMESS/VLESS/TROJAN/SS/SOCKS/HTTP/WireGuard), and persists valid profiles.
 *
 * - importQrCode(frameData):
 *     Decodes a QR code from the provided frame (raw bytes + dimensions/rotation/format),
 *     parses supported protocols, and persists valid profiles.
 */
interface SubscriptionRepository {
    suspend fun importClipboard(): ImportResult
    suspend fun importQrCode(frameData: FrameData): ImportResult
}
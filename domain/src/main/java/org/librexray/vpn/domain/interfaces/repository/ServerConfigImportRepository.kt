package org.librexray.vpn.domain.interfaces.repository

import org.librexray.vpn.domain.models.FrameData
import org.librexray.vpn.domain.models.ImportResult

/**
 * Imports configuration profiles from user-facing sources (clipboard, QR frames).
 *
 * Contracts:
 * - importFromClipboard():
 *     Reads textual content from the system clipboard, parses supported protocol URIs
 *     (e.g., VMESS/VLESS/TROJAN/SS/SOCKS/HTTP/WireGuard), and persists valid profiles.
 *
 * - importFromQrFrame(frameData):
 *     Decodes a QR code from the provided frame (raw bytes + dimensions/rotation/format),
 *     parses supported protocols, and persists valid profiles.
 */
interface ServerConfigImportRepository {
    suspend fun importFromClipboard(): ImportResult
    suspend fun importFromQrFrame(frameData: FrameData): ImportResult
}
package com.pet.vpn_client.domain.models

/**
 * Result of importing VPN configurations from an external source (clipboard, QR, etc.).
 *
 * Exactly three outcomes are represented:
 * - [Success]: input was valid and at least one configuration was imported.
 * - [Empty]: import completed but produced zero configurations (no usable input).
 * - [Error]: the import failed due to an unexpected error (I/O, parsing, etc.).
 */
sealed interface ImportResult {
    object Error: ImportResult
    object Empty: ImportResult
    object Success: ImportResult
}
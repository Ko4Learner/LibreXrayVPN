package com.pet.vpn_client.data.repository_impl

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.data.protocol_parsers.ShadowsocksParser
import com.pet.vpn_client.data.protocol_parsers.SocksParser
import com.pet.vpn_client.data.protocol_parsers.TrojanParser
import com.pet.vpn_client.data.protocol_parsers.VlessParser
import com.pet.vpn_client.data.protocol_parsers.VmessParser
import com.pet.vpn_client.data.protocol_parsers.WireguardParser
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.ServerConfigImportRepository
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.domain.models.FrameData
import com.pet.vpn_client.domain.models.ImportResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Imports VPN server configurations from user-provided sources (clipboard, QR code)
 * and persists them via [KeyValueStorage].
 */
class ServerConfigImportRepositoryImpl @Inject constructor(
    private val storage: KeyValueStorage,
    private val shadowsocksParser: ShadowsocksParser,
    private val socksParser: SocksParser,
    private val trojanParser: TrojanParser,
    private val vlessParser: VlessParser,
    private val vmessParser: VmessParser,
    private val wireguardParser: WireguardParser,
    @ApplicationContext private val context: Context
) : ServerConfigImportRepository {
    /**
     * Imports configuration(s) from the system clipboard.
     */
    override suspend fun importFromClipboard(): ImportResult = try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cm.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
            ?.trim()
            .orEmpty()
        if (text.isEmpty()) {
            ImportResult.Empty
        } else {
            withContext(Dispatchers.IO) { importBatchConfig(text) }
        }
    } catch (c: CancellationException) {
        throw c
    } catch (e: SecurityException) {
        Log.e(Constants.TAG, "Clipboard access denied", e)
        ImportResult.Error
    } catch (e: Exception) {
        Log.e(Constants.TAG, "Failed to import config from clipboard", e)
        ImportResult.Error
    }

    /**
     * Imports configuration(s) from a QR code image frame.
     *
     * 1) Builds an [InputImage] from [frameData] (expects valid rotation 0/90/180/270 and image format).
     * 2) Processes the image with ML Kit BarcodeScanner and obtains the first barcode value.
     */
    override suspend fun importFromQrFrame(frameData: FrameData): ImportResult {
        val image = InputImage.fromByteArray(
            frameData.bytes,
            frameData.width,
            frameData.height,
            frameData.rotationDegrees,  // ensure 0/90/180/270 upstream
            frameData.imageFormat
        )
        val scanner = BarcodeScanning.getClient()
        return try {
            val barcodes = scanner.process(image).await()
            val raw = barcodes.firstOrNull()?.rawValue?.trim().orEmpty()
            if (raw.isEmpty()) {
                ImportResult.Empty
            } else {
                withContext(Dispatchers.IO) { importBatchConfig(raw) }
            }
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to import config from QR", e)
            ImportResult.Error
        } finally {
            try {
                scanner.close()
            } catch (_: Exception) { /* ignore */
            }
        }
    }

    /**
     * Attempts to parse and persist a batch of configs.
     */
    private fun importBatchConfig(server: String?): ImportResult {
        return runCatching {
            val addedDecoded = parseBatchConfig(Utils.decode(server))
            val added = if (addedDecoded == 0) parseBatchConfig(server) else addedDecoded
            if (added > 0) ImportResult.Success else ImportResult.Empty
        }.getOrElse { e ->
            Log.e(Constants.TAG, "Failed to import from text", e)
            ImportResult.Error
        }
    }

    /**
     * Parses multiple configuration lines and persists valid entries.
     *
     * Input format:
     * - `servers` may contain multiple lines; each line is processed independently.
     */
    private fun parseBatchConfig(servers: String?): Int {
        if (servers.isNullOrBlank()) return 0

        val lines = servers
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
            .asReversed()

        var added = 0
        for (line in lines) {
            if (parseConfig(line)) {
                added++
            }
        }
        return added
    }

    /**
     * Parses a single config string for supported protocols and persists it if valid.
     */
    private fun parseConfig(
        str: String
    ): Boolean {
        if (str.isBlank()) return false

        val config = when {
            str.startsWith(ConfigType.VMESS.protocolScheme) -> vmessParser.parse(str)
            str.startsWith(ConfigType.SHADOWSOCKS.protocolScheme) -> shadowsocksParser.parse(str)
            str.startsWith(ConfigType.SOCKS.protocolScheme) -> socksParser.parse(str)
            str.startsWith(ConfigType.TROJAN.protocolScheme) -> trojanParser.parse(str)
            str.startsWith(ConfigType.VLESS.protocolScheme) -> vlessParser.parse(str)
            str.startsWith(ConfigType.WIREGUARD.protocolScheme) -> wireguardParser.parse(str)
            else -> null
        } ?: return false

        val guid = storage.encodeServerConfig("", config)
        storage.setSelectedServer(guid)
        return true
    }
}
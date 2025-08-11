package com.pet.vpn_client.data.repository_impl

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
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
import com.pet.vpn_client.domain.interfaces.repository.SubscriptionRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
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
class SubscriptionRepositoryImpl @Inject constructor(
    private val storage: KeyValueStorage,
    private val shadowsocksParser: ShadowsocksParser,
    private val socksParser: SocksParser,
    private val trojanParser: TrojanParser,
    private val vlessParser: VlessParser,
    private val vmessParser: VmessParser,
    private val wireguardParser: WireguardParser,
    @ApplicationContext private val context: Context
) : SubscriptionRepository {
    //TODO уйти от цифр в результатах
    /**
     * Imports configuration(s) from the system clipboard.
     */
    override suspend fun importClipboard(): ImportResult = try {
        val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cmb.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
            ?.trim()
            .orEmpty()

        when {
            text.isEmpty() -> ImportResult.Empty
            importBatchConfig(text) > 0 -> ImportResult.Success
            else -> ImportResult.Empty
        }
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
    override suspend fun importQrCode(frameData: FrameData): ImportResult {
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
                val imported = withContext(Dispatchers.IO) { importBatchConfig(raw) }
                if (imported > 0) ImportResult.Success else ImportResult.Empty
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
     * @return Number of successfully parsed & stored entries (>0 means success for callers).
     */
    private fun importBatchConfig(server: String?): Int {
        var count = parseBatchConfig(Utils.decode(server))
        if (count <= 0) {
            count = parseBatchConfig(server)
        }
        return count
    }

    /**
     * Parses multiple configuration lines and persists valid entries.
     *
     * Input format:
     * - `servers` may contain multiple lines; each line is processed independently.
     */
    private fun parseBatchConfig(servers: String?): Int {
        try {
            if (servers == null) {
                return 0
            }
            //TODO нужно для добавления или нет в выбранный сервер
            val removedSelectedServer = null

            var count = 0
            servers.lines()
                .distinct()
                .reversed()
                .forEach {
                    val resId = parseConfig(it, removedSelectedServer)
                    if (resId == 0) {
                        count++
                    }
                }
            return count
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to parse batch config", e)
        }
        return 0
    }

    /**
     * Parses a single config string for supported protocols and persists it if valid.
     */
    private fun parseConfig(
        str: String?,
        removedSelectedServer: ConfigProfileItem?
    ): Int {
        try {
            if (str == null || TextUtils.isEmpty(str)) {
                return 1
            }

            val config = if (str.startsWith(EConfigType.VMESS.protocolScheme)) {
                vmessParser.parse(str)
            } else if (str.startsWith(EConfigType.SHADOWSOCKS.protocolScheme)) {
                shadowsocksParser.parse(str)
            } else if (str.startsWith(EConfigType.SOCKS.protocolScheme)) {
                socksParser.parse(str)
            } else if (str.startsWith(EConfigType.TROJAN.protocolScheme)) {
                trojanParser.parse(str)
            } else if (str.startsWith(EConfigType.VLESS.protocolScheme)) {
                vlessParser.parse(str)
            } else if (str.startsWith(EConfigType.WIREGUARD.protocolScheme)) {
                wireguardParser.parse(str)
            } else {
                null
            }

            if (config == null) {
                return 1
            }

            //TODO все сервера имеют один subId
            config.subscriptionId = "1"
            //TODO при добавлении разобраться когда менять выбранный сервер а когда нет
            val guid = storage.encodeServerConfig("", config)
            if (removedSelectedServer != null &&
                config.server == removedSelectedServer.server && config.serverPort == removedSelectedServer.serverPort
            ) {
                storage.setSelectedServer(guid)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to parse config", e)
            return -1
        }
        return 0
    }
}
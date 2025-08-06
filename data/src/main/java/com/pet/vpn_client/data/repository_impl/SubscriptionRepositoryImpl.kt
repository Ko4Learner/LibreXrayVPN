package com.pet.vpn_client.data.repository_impl

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.data.config_formatter.ShadowsocksFormatter
import com.pet.vpn_client.data.config_formatter.SocksFormatter
import com.pet.vpn_client.data.config_formatter.TrojanFormatter
import com.pet.vpn_client.data.config_formatter.VlessFormatter
import com.pet.vpn_client.data.config_formatter.VmessFormatter
import com.pet.vpn_client.data.config_formatter.WireguardFormatter
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.SubscriptionRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.FrameData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class SubscriptionRepositoryImpl @Inject constructor(
    val storage: KeyValueStorage,
    val gson: Gson,
    val configRepository: ConfigRepository,
    val shadowsocksFormatter: ShadowsocksFormatter,
    val socksFormatter: SocksFormatter,
    val trojanFormatter: TrojanFormatter,
    val vlessFormatter: VlessFormatter,
    val vmessFormatter: VmessFormatter,
    val wireguardFormatter: WireguardFormatter,
    @ApplicationContext val context: Context
) : SubscriptionRepository {
    override suspend fun importClipboard(): Int {
        try {
            val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipboard = cmb.primaryClip?.getItemAt(0)?.text.toString()
            val count = importBatchConfig(
                clipboard
            )
            return count
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to import config from clipboard", e)
            return -1
        }
    }

    override suspend fun importQrCode(frameData: FrameData): Int =
        suspendCancellableCoroutine { cont ->
            val inputImage = InputImage.fromByteArray(
                frameData.bytes,
                frameData.width,
                frameData.height,
                frameData.rotationDegrees,
                frameData.imageFormat
            )

            val scanner = BarcodeScanning.getClient()

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val result = barcodes.firstOrNull()?.rawValue
                    if (result != null) {
                        try {
                            val count = importBatchConfig(result)
                            cont.resume(count)
                        } catch (_: Exception) {
                            cont.resume(-1)
                        }
                    } else {
                        cont.resume(-1)
                    }
                }
                .addOnFailureListener {
                    cont.resume(-1)
                }
        }

    private fun importBatchConfig(server: String?): Int {
        var count = parseBatchConfig(Utils.decode(server))
        if (count <= 0) {
            count = parseBatchConfig(server)
        }
        return count
    }

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

    private fun parseConfig(
        str: String?,
        removedSelectedServer: ConfigProfileItem?
    ): Int {
        try {
            if (str == null || TextUtils.isEmpty(str)) {
                return 1
            }

            val config = if (str.startsWith(EConfigType.VMESS.protocolScheme)) {
                vmessFormatter.parse(str)
            } else if (str.startsWith(EConfigType.SHADOWSOCKS.protocolScheme)) {
                shadowsocksFormatter.parse(str)
            } else if (str.startsWith(EConfigType.SOCKS.protocolScheme)) {
                socksFormatter.parse(str)
            } else if (str.startsWith(EConfigType.TROJAN.protocolScheme)) {
                trojanFormatter.parse(str)
            } else if (str.startsWith(EConfigType.VLESS.protocolScheme)) {
                vlessFormatter.parse(str)
            } else if (str.startsWith(EConfigType.WIREGUARD.protocolScheme)) {
                wireguardFormatter.parse(str)
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
                storage.setSelectServer(guid)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to parse config", e)
            return -1
        }
        return 0
    }
}
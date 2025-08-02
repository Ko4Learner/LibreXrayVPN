package com.pet.vpn_client.data.repository_impl

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
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.SubscriptionManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.FrameData
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class SubscriptionManagerImpl @Inject constructor(
    val storage: KeyValueStorage,
    val gson: Gson,
    val configManager: ConfigManager,
    val shadowsocksFormatter: ShadowsocksFormatter,
    val socksFormatter: SocksFormatter,
    val trojanFormatter: TrojanFormatter,
    val vlessFormatter: VlessFormatter,
    val vmessFormatter: VmessFormatter,
    val wireguardFormatter: WireguardFormatter,
    val context: Context
) : SubscriptionManager {

    override suspend fun importClipboard(): Int {
        try {
            val clipboard = Utils.getClipboard(context)
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
            //TODO добавить проверку наличия серверов
            val removedSelectedServer =  null

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

    //TODO убрать subItem
    private fun parseConfig(
        str: String?,
        removedSelectedServer: ConfigProfileItem?
    ): Int {
        try {
            if (str == null || TextUtils.isEmpty(str)) {
                return /*R.string.toast_none_data*/ 1
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
                return /*R.string.toast_incorrect_protocol*/ 1
            }

            //TODO все сервера имеют один subId
            config.subscriptionId = "1"
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
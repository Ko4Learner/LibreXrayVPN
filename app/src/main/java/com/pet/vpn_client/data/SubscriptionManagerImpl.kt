package com.pet.vpn_client.data

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.data.config_formatter.HttpFormatter
import com.pet.vpn_client.data.config_formatter.ShadowsocksFormatter
import com.pet.vpn_client.data.config_formatter.SocksFormatter
import com.pet.vpn_client.data.config_formatter.TrojanFormatter
import com.pet.vpn_client.data.config_formatter.VlessFormatter
import com.pet.vpn_client.data.config_formatter.VmessFormatter
import com.pet.vpn_client.data.config_formatter.WireguardFormatter
import com.pet.vpn_client.data.qr_code.QRCodeDecoder
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.domain.interfaces.SubscriptionManager
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.SubscriptionItem
import com.pet.vpn_client.utils.HttpUtil
import com.pet.vpn_client.utils.Utils
import java.net.URI
import javax.inject.Inject

class SubscriptionManagerImpl @Inject constructor(
    val storage: KeyValueStorage,
    val gson: Gson,
    val settingsManager: SettingsManager,
    val configManager: ConfigManager,
    val httpFormatter: HttpFormatter,
    val shadowsocksFormatter: ShadowsocksFormatter,
    val socksFormatter: SocksFormatter,
    val trojanFormatter: TrojanFormatter,
    val vlessFormatter: VlessFormatter,
    val vmessFormatter: VmessFormatter,
    val wireguardFormatter: WireguardFormatter,
    val qrCodeDecoder: QRCodeDecoder,
    val context: Context
) : SubscriptionManager {

    fun shareToClipboard(guid: String): Int {
        try {
            val conf = shareConfig(guid)
            if (TextUtils.isEmpty(conf)) {
                return -1
            }

            Utils.setClipboard(context, conf)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to share config to clipboard", e)
            return -1
        }
        return 0
    }

    fun shareNonCustomConfigsToClipboard(serverList: List<String>): Int {
        try {
            val sb = StringBuilder()
            for (guid in serverList) {
                val url = shareConfig(guid)
                if (TextUtils.isEmpty(url)) {
                    continue
                }
                sb.append(url)
                sb.appendLine()
            }
            if (sb.count() > 0) {
                Utils.setClipboard(context, sb.toString())
            }
            return sb.lines().count()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to share non-custom configs to clipboard", e)
            return -1
        }
    }

    fun shareToQRCode(guid: String): Bitmap? {
        try {
            val conf = shareConfig(guid)
            if (TextUtils.isEmpty(conf)) {
                return null
            }
            return qrCodeDecoder.createQRCode(conf)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to share config as QR code", e)
            return null
        }
    }

    fun shareFullContentToClipboard(guid: String?): Int {
        try {
            if (guid == null) return -1
            val result = configManager.getCoreConfig(guid)
            if (result.status) {
                Utils.setClipboard(context, result.content)
            } else {
                return -1
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to share full content to clipboard", e)
            return -1
        }
        return 0
    }

    private fun shareConfig(guid: String): String {
        try {
            val config = storage.decodeServerConfig(guid) ?: return ""

            return config.configType.protocolScheme + when (config.configType) {
                EConfigType.VMESS -> vmessFormatter.toUri(config)
                EConfigType.SHADOWSOCKS -> shadowsocksFormatter.toUri(config)
                EConfigType.SOCKS -> socksFormatter.toUri(config)
                EConfigType.HTTP -> ""
                EConfigType.VLESS -> vlessFormatter.toUri(config)
                EConfigType.TROJAN -> trojanFormatter.toUri(config)
                EConfigType.WIREGUARD -> wireguardFormatter.toUri(config)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to share config for GUID: $guid", e)
            return ""
        }
    }

    override suspend fun importClipboard()
            : Boolean {
        try {
            val clipboard = Utils.getClipboard(context)
            importBatchConfig(
                clipboard,
                storage.decodeSettingsString(Constants.CACHE_SUBSCRIPTION_ID, "").orEmpty(),
                true
            )
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to import config from clipboard", e)
            return false
        }
        return true
    }

    override suspend fun importBatchConfig(
        server: String?,
        subid: String,
        append: Boolean
    ): Pair<Int, Int> {
        val clipboard = Utils.getClipboard(context)
        var count = parseBatchConfig(Utils.decode(server), subid, append)
        if (count <= 0) {
            count = parseBatchConfig(server, subid, append)
        }
//        if (count <= 0) {
//            count = parseCustomConfigServer(server, subid)
//        }

        var countSub = parseBatchSubscription(server)
        if (countSub <= 0) {
            countSub = parseBatchSubscription(Utils.decode(server))
        }
        if (countSub > 0) {
            updateConfigViaSubAll()
        }

        return count to countSub
    }

    private fun parseBatchSubscription(servers: String?): Int {
        try {
            if (servers == null) {
                return 0
            }

            var count = 0
            servers.lines()
                .distinct()
                .forEach { str ->
                    if (Utils.isValidSubUrl(str)) {
                        count += importUrlAsSubscription(str)
                    }
                }
            return count
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to parse batch subscription", e)
        }
        return 0
    }

    private fun parseBatchConfig(servers: String?, subid: String, append: Boolean): Int {
        try {
            if (servers == null) {
                return 0
            }
            val removedSelectedServer =
                if (!TextUtils.isEmpty(subid) && !append) {
                    storage.decodeServerConfig(
                        storage.getSelectServer().orEmpty()
                    )?.let {
                        if (it.subscriptionId == subid) {
                            return@let it
                        }
                        return@let null
                    }
                } else {
                    null
                }
            if (!append) {
                storage.removeServerViaSubId(subid)
            }

            val subItem = storage.decodeSubscription(subid)
            var count = 0
            servers.lines()
                .distinct()
                .reversed()
                .forEach {
                    val resId = parseConfig(it, subid, subItem, removedSelectedServer)
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

//    private fun parseCustomConfigServer(server: String?, subid: String): Int {
//        if (server == null) {
//            return 0
//        }
//        if (server.contains("inbounds")
//            && server.contains("outbounds")
//            && server.contains("routing")
//        ) {
//            try {
//                val serverList: Array<Any> =
//                    gson.fromJson(server, Array<Any>::class.java)
//
//                if (serverList.isNotEmpty()) {
//                    var count = 0
//                    for (srv in serverList.reversed()) {
//                        val config = CustomFmt.parse(gson.toJson(srv)) ?: continue
//                        config.subscriptionId = subid
//                        val key = storage.encodeServerConfig("", config)
//                        storage.encodeServerRaw(key, JsonUtil.toJsonPretty(srv) ?: "")
//                        count += 1
//                    }
//                    return count
//                }
//            } catch (e: Exception) {
//                Log.e(Constants.TAG, "Failed to parse custom config server JSON array", e)
//            }
//
//            try {
//                // For compatibility
//                val config = CustomFmt.parse(server) ?: return 0
//                config.subscriptionId = subid
//                val key = storage.encodeServerConfig("", config)
//                storage.encodeServerRaw(key, server)
//                return 1
//            } catch (e: Exception) {
//                Log.e(Constants.TAG, "Failed to parse custom config server as single config", e)
//            }
//            return 0
//        } else if (server.startsWith("[Interface]") && server.contains("[Peer]")) {
//            try {
//                val config = wireguardFormatter.parseWireguardConfFile(server)
//                    ?: return R.string.toast_incorrect_protocol
//                val key = storage.encodeServerConfig("", config)
//                storage.encodeServerRaw(key, server)
//                return 1
//            } catch (e: Exception) {
//                Log.e(Constants.TAG, "Failed to parse WireGuard config file", e)
//            }
//            return 0
//        } else {
//            return 0
//        }
//    }

    private fun parseConfig(
        str: String?,
        subid: String,
        subItem: SubscriptionItem?,
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
            //filter
            if (subItem?.filter != null && subItem.filter?.isNotEmpty() == true && config.remarks.isNotEmpty()) {
                val matched = Regex(pattern = subItem.filter ?: "")
                    .containsMatchIn(input = config.remarks)
                if (!matched) return -1
            }

            config.subscriptionId = subid
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

    fun updateConfigViaSubAll(): Int {
        var count = 0
        try {
            storage.decodeSubscriptions().forEach {
                count += updateConfigViaSub(it)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to update config via all subscriptions", e)
            return 0
        }
        return count
    }

    fun updateConfigViaSub(it: Pair<String, SubscriptionItem>): Int {
        try {
            if (TextUtils.isEmpty(it.first)
                || TextUtils.isEmpty(it.second.remarks)
                || TextUtils.isEmpty(it.second.url)
            ) {
                return 0
            }
            if (!it.second.enabled) {
                return 0
            }
            val url = HttpUtil.idnToASCII(it.second.url)
            if (!Utils.isValidUrl(url)) {
                return 0
            }
            if (!Utils.isValidSubUrl(url)) {
                return 0
            }
            Log.i(Constants.TAG, url)

            var configText = try {
                val httpPort = settingsManager.getHttpPort()
                HttpUtil.getUrlContentWithUserAgent(url, 15000, httpPort)
            } catch (e: Exception) {
                Log.e(
                    Constants.ANG_PACKAGE,
                    "Update subscription: proxy not ready or other error",
                    e
                )
                ""
            }
            if (configText.isEmpty()) {
                configText = try {
                    HttpUtil.getUrlContentWithUserAgent(url)
                } catch (e: Exception) {
                    Log.e(
                        Constants.TAG,
                        "Update subscription: Failed to get URL content with user agent",
                        e
                    )
                    ""
                }
            }
            if (configText.isEmpty()) {
                return 0
            }
            return parseConfigViaSub(configText, it.first, false)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to update config via subscription", e)
            return 0
        }
    }

    private fun parseConfigViaSub(server: String?, subid: String, append: Boolean): Int {
        var count = parseBatchConfig(Utils.decode(server), subid, append)
        if (count <= 0) {
            count = parseBatchConfig(server, subid, append)
        }
//        if (count <= 0) {
//            count = parseCustomConfigServer(server, subid)
//        }
        return count
    }

    private fun importUrlAsSubscription(url: String): Int {
        val subscriptions = storage.decodeSubscriptions()
        subscriptions.forEach {
            if (it.second.url == url) {
                return 0
            }
        }
        val uri = URI(Utils.fixIllegalUrl(url))
        val subItem = SubscriptionItem()
        subItem.remarks = uri.fragment ?: "import sub"
        subItem.url = url
        storage.encodeSubscription("", subItem)
        return 1
    }
}
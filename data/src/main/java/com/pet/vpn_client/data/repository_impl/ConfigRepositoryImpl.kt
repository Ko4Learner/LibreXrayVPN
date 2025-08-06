package com.pet.vpn_client.data.repository_impl

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.XrayConfig
import com.pet.vpn_client.data.config_formatter.HttpFormatter
import com.pet.vpn_client.data.config_formatter.ShadowsocksFormatter
import com.pet.vpn_client.data.config_formatter.SocksFormatter
import com.pet.vpn_client.data.config_formatter.TrojanFormatter
import com.pet.vpn_client.data.config_formatter.VlessFormatter
import com.pet.vpn_client.data.config_formatter.VmessFormatter
import com.pet.vpn_client.data.config_formatter.WireguardFormatter
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Type
import java.net.Inet6Address
import java.net.InetAddress
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    val storage: KeyValueStorage,
    val gson: Gson,
    val httpFormatter: HttpFormatter,
    val shadowsocksFormatter: ShadowsocksFormatter,
    val socksFormatter: SocksFormatter,
    val trojanFormatter: TrojanFormatter,
    val vlessFormatter: VlessFormatter,
    val vmessFormatter: VmessFormatter,
    val wireguardFormatter: WireguardFormatter,
    @ApplicationContext val context: Context
) : ConfigRepository {
    private var initConfigCache: String? = null

    override fun getCoreConfig(guid: String): ConfigResult {
        try {
            val config = storage.decodeServerConfig(guid) ?: return ConfigResult(false)
            return getXrayNormalConfig(context, guid, config)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get Xray config", e)
            return ConfigResult(false)
        }
    }

    private fun getXrayNormalConfig(
        context: Context,
        guid: String,
        config: ConfigProfileItem
    ): ConfigResult {
        val result = ConfigResult(false)

        val address = config.server ?: return result
        if (!Utils.isIpAddress(address)) {
            if (!Utils.isValidUrl(address)) {
                Log.w(Constants.TAG, "$address is an invalid ip or domain")
                return result
            }
        }

        val xRayConfig = initXrayConfig(context) ?: return result
        xRayConfig.log.loglevel = LOG_LEVEL
        xRayConfig.remarks = config.remarks
        xRayConfig.routing.domainStrategy = "IPIfNonMatch"

        getInbounds(xRayConfig)

        getOutbounds(xRayConfig, config) ?: return result

        getDns(xRayConfig)

        resolveOutboundDomainsToHosts(xRayConfig)

        result.status = true
        result.content = toJsonPretty(xRayConfig) ?: ""
        result.guid = guid
        return result
    }

    private fun initXrayConfig(context: Context): XrayConfig? {
        val assets = initConfigCache ?: readTextFromAssets(context)
        if (TextUtils.isEmpty(assets)) {
            return null
        }
        initConfigCache = assets
        val config = gson.fromJson(assets, XrayConfig::class.java)
        return config
    }

    private fun getInbounds(xrayConfig: XrayConfig): Boolean {
        try {
            xrayConfig.inbounds.forEach { curInbound ->
                curInbound.listen = LOOPBACK
            }
            xrayConfig.inbounds[0].port = 10808
            xrayConfig.inbounds[0].sniffing?.enabled = true
            xrayConfig.inbounds[0].sniffing?.routeOnly = false
            xrayConfig.inbounds.removeAt(1)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure inbounds", e)
            return false
        }
        return true
    }

    private fun getOutbounds(xrayConfig: XrayConfig, config: ConfigProfileItem): Boolean? {
        val outbound = convertProfile2Outbound(config) ?: return null
        val ret = updateOutboundWithGlobalSettings(outbound)
        if (!ret) return null

        if (xrayConfig.outbounds.isNotEmpty()) {
            xrayConfig.outbounds[0] = outbound
        } else {
            xrayConfig.outbounds.add(outbound)
        }
        return true
    }

    private fun getDns(xrayConfig: XrayConfig): Boolean {
        try {
            val hosts = mapOf(
                GOOGLEAPIS_CN_DOMAIN to GOOGLEAPIS_COM_DOMAIN,
                DNS_ALIDNS_DOMAIN to DNS_ALIDNS_ADDRESSES,
                DNS_CLOUDFLARE_DOMAIN to DNS_CLOUDFLARE_ADDRESSES,
                DNS_DNSPOD_DOMAIN to DNS_DNSPOD_ADDRESSES,
                DNS_GOOGLE_DOMAIN to DNS_GOOGLE_ADDRESSES,
                DNS_QUAD9_DOMAIN to DNS_QUAD9_ADDRESSES,
                DNS_YANDEX_DOMAIN to DNS_YANDEX_ADDRESSES
            )
            val servers = arrayListOf<Any>(DNS_PROXY)

            xrayConfig.dns = XrayConfig.DnsBean(
                servers = servers,
                hosts = hosts
            )
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure DNS", e)
            return false
        }
        return true
    }

    private fun updateOutboundWithGlobalSettings(outbound: XrayConfig.OutboundBean): Boolean {
        try {
            val protocol = outbound.protocol
            outbound.mux?.enabled = false
            outbound.mux?.concurrency = -1

            if (protocol.equals(EConfigType.WIREGUARD.name, true)) {
                var localTunAdd = if (outbound.settings?.address == null) {
                    listOf(Constants.WIREGUARD_LOCAL_ADDRESS_V4)
                } else {
                    outbound.settings?.address as List<*>
                }
                localTunAdd = listOf(localTunAdd.first())
                outbound.settings?.address = localTunAdd
            }

            if (outbound.streamSettings?.network == Constants.DEFAULT_NETWORK
                && outbound.streamSettings?.tcpSettings?.header?.type == HEADER_TYPE_HTTP
            ) {
                val path = outbound.streamSettings?.tcpSettings?.header?.request?.path
                val host = outbound.streamSettings?.tcpSettings?.header?.request?.headers?.Host

                val requestString: String by lazy {
                    STEALTH_HTTP_HEADERS_JSON
                }
                outbound.streamSettings?.tcpSettings?.header?.request = gson.fromJson(
                    requestString,
                    XrayConfig.OutboundBean.StreamSettingsBean.TcpSettingsBean.HeaderBean.RequestBean::class.java
                )
                outbound.streamSettings?.tcpSettings?.header?.request?.path =
                    if (path.isNullOrEmpty()) {
                        listOf("/")
                    } else {
                        path
                    }
                outbound.streamSettings?.tcpSettings?.header?.request?.headers?.Host = host
            }


        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to update outbound with global settings", e)
            return false
        }
        return true
    }


    private fun resolveOutboundDomainsToHosts(xrayConfig: XrayConfig) {
        val proxyOutboundList = xrayConfig.getAllProxyOutbound()
        val dns = xrayConfig.dns ?: return
        val newHosts = dns.hosts?.toMutableMap() ?: mutableMapOf()

        for (item in proxyOutboundList) {
            val domain = item.getServerAddress()
            if (domain.isNullOrEmpty()) continue
            if (newHosts.containsKey(domain)) continue

            val resolvedIps = resolveHostToIP(domain)
            if (resolvedIps.isNullOrEmpty()) continue

            item.ensureSockopt().domainStrategy = "UseIPv4v6"
            newHosts[domain] = if (resolvedIps.size == 1) {
                resolvedIps[0]
            } else {
                resolvedIps
            }
        }

        dns.hosts = newHosts
    }

    private fun convertProfile2Outbound(profileItem: ConfigProfileItem): XrayConfig.OutboundBean? {
        return when (profileItem.configType) {
            EConfigType.VMESS -> vmessFormatter.toOutbound(profileItem)
            EConfigType.SHADOWSOCKS -> shadowsocksFormatter.toOutbound(profileItem)
            EConfigType.SOCKS -> socksFormatter.toOutbound(profileItem)
            EConfigType.VLESS -> vlessFormatter.toOutbound(profileItem)
            EConfigType.TROJAN -> trojanFormatter.toOutbound(profileItem)
            EConfigType.WIREGUARD -> wireguardFormatter.toOutbound(profileItem)
            EConfigType.HTTP -> httpFormatter.toOutbound(profileItem)
        }
    }

    override fun createInitOutbound(configType: EConfigType): XrayConfig.OutboundBean? {
        return when (configType) {
            EConfigType.VMESS, EConfigType.VLESS -> XrayConfig.OutboundBean(
                protocol = configType.name.lowercase(),
                settings = XrayConfig.OutboundBean.OutSettingsBean(
                    vnext = listOf(
                        XrayConfig.OutboundBean.OutSettingsBean.VnextBean(
                            users = listOf(XrayConfig.OutboundBean.OutSettingsBean.VnextBean.UsersBean())
                        )
                    )
                ), streamSettings = XrayConfig.OutboundBean.StreamSettingsBean()
            )

            EConfigType.SHADOWSOCKS,
            EConfigType.SOCKS,
            EConfigType.HTTP,
            EConfigType.TROJAN -> XrayConfig.OutboundBean(
                protocol = configType.name.lowercase(),
                settings = XrayConfig.OutboundBean.OutSettingsBean(
                    servers = listOf(XrayConfig.OutboundBean.OutSettingsBean.ServersBean())
                ),
                streamSettings = XrayConfig.OutboundBean.StreamSettingsBean()
            )

            EConfigType.WIREGUARD -> XrayConfig.OutboundBean(
                protocol = configType.name.lowercase(),
                settings = XrayConfig.OutboundBean.OutSettingsBean(
                    secretKey = "",
                    peers = listOf(XrayConfig.OutboundBean.OutSettingsBean.WireGuardBean())
                )
            )
        }
    }

    override fun populateTransportSettings(
        streamSettings: XrayConfig.OutboundBean.StreamSettingsBean,
        profileItem: ConfigProfileItem
    ): String? {
        val transport = profileItem.network.orEmpty()
        val headerType = profileItem.headerType
        val host = profileItem.host
        val path = profileItem.path
        val seed = profileItem.seed
        val mode = profileItem.mode
        val serviceName = profileItem.serviceName
        val authority = profileItem.authority
        val xhttpMode = profileItem.xhttpMode
        val xhttpExtra = profileItem.xhttpExtra

        var sni: String? = null
        streamSettings.network = transport.ifEmpty { NetworkType.TCP.type }
        when (streamSettings.network) {
            NetworkType.TCP.type -> {
                val tcpSetting = XrayConfig.OutboundBean.StreamSettingsBean.TcpSettingsBean()
                if (headerType == HEADER_TYPE_HTTP) {
                    tcpSetting.header.type = HEADER_TYPE_HTTP
                    if (!TextUtils.isEmpty(host) || !TextUtils.isEmpty(path)) {
                        val requestObj =
                            XrayConfig.OutboundBean.StreamSettingsBean.TcpSettingsBean.HeaderBean.RequestBean()
                        requestObj.headers.Host =
                            host.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        requestObj.path =
                            path.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        tcpSetting.header.request = requestObj
                        sni = requestObj.headers.Host?.getOrNull(0)
                    }
                } else {
                    tcpSetting.header.type = "none"
                    sni = host
                }
                streamSettings.tcpSettings = tcpSetting
            }

            NetworkType.KCP.type -> {
                val kcpsetting = XrayConfig.OutboundBean.StreamSettingsBean.KcpSettingsBean()
                kcpsetting.header.type = headerType ?: "none"
                if (seed.isNullOrEmpty()) {
                    kcpsetting.seed = null
                } else {
                    kcpsetting.seed = seed
                }
                if (host.isNullOrEmpty()) {
                    kcpsetting.header.domain = null
                } else {
                    kcpsetting.header.domain = host
                }
                streamSettings.kcpSettings = kcpsetting
            }

            NetworkType.WS.type -> {
                val wssetting = XrayConfig.OutboundBean.StreamSettingsBean.WsSettingsBean()
                wssetting.headers.Host = host.orEmpty()
                sni = host
                wssetting.path = path ?: "/"
                streamSettings.wsSettings = wssetting
            }

            NetworkType.HTTP_UPGRADE.type -> {
                val httpupgradeSetting =
                    XrayConfig.OutboundBean.StreamSettingsBean.HttpupgradeSettingsBean()
                httpupgradeSetting.host = host.orEmpty()
                sni = host
                httpupgradeSetting.path = path ?: "/"
                streamSettings.httpupgradeSettings = httpupgradeSetting
            }

            NetworkType.XHTTP.type -> {
                val xhttpSetting = XrayConfig.OutboundBean.StreamSettingsBean.XhttpSettingsBean()
                xhttpSetting.host = host.orEmpty()
                sni = host
                xhttpSetting.path = path ?: "/"
                xhttpSetting.mode = xhttpMode
                xhttpSetting.extra = parseString(xhttpExtra)
                streamSettings.xhttpSettings = xhttpSetting
            }

            NetworkType.H2.type, NetworkType.HTTP.type -> {
                streamSettings.network = NetworkType.H2.type
                val h2Setting = XrayConfig.OutboundBean.StreamSettingsBean.HttpSettingsBean()
                h2Setting.host =
                    host.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                sni = h2Setting.host.getOrNull(0)
                h2Setting.path = path ?: "/"
                streamSettings.httpSettings = h2Setting
            }

            NetworkType.GRPC.type -> {
                val grpcSetting = XrayConfig.OutboundBean.StreamSettingsBean.GrpcSettingsBean()
                grpcSetting.multiMode = mode == "multi"
                grpcSetting.serviceName = serviceName.orEmpty()
                grpcSetting.authority = authority.orEmpty()
                grpcSetting.idle_timeout = 60
                grpcSetting.health_check_timeout = 20
                sni = authority
                streamSettings.grpcSettings = grpcSetting
            }
        }
        return sni
    }

    override fun populateTlsSettings(
        streamSettings: XrayConfig.OutboundBean.StreamSettingsBean,
        profileItem: ConfigProfileItem,
        sniExt: String?
    ) {
        val streamSecurity = profileItem.security.orEmpty()
        val allowInsecure = profileItem.insecure == true
        val sni = if (profileItem.sni.isNullOrEmpty()) sniExt else profileItem.sni
        val fingerprint = profileItem.fingerPrint
        val alpns = profileItem.alpn
        val publicKey = profileItem.publicKey
        val shortId = profileItem.shortId
        val spiderX = profileItem.spiderX

        streamSettings.security = streamSecurity.ifEmpty { null }
        if (streamSettings.security == null) return
        val tlsSetting = XrayConfig.OutboundBean.StreamSettingsBean.TlsSettingsBean(
            allowInsecure = allowInsecure,
            serverName = if (sni.isNullOrEmpty()) null else sni,
            fingerprint = if (fingerprint.isNullOrEmpty()) null else fingerprint,
            alpn = if (alpns.isNullOrEmpty()) null else alpns.split(",").map { it.trim() }
                .filter { it.isNotEmpty() },
            publicKey = if (publicKey.isNullOrEmpty()) null else publicKey,
            shortId = if (shortId.isNullOrEmpty()) null else shortId,
            spiderX = if (spiderX.isNullOrEmpty()) null else spiderX,
        )
        if (streamSettings.security == Constants.TLS) {
            streamSettings.tlsSettings = tlsSetting
            streamSettings.realitySettings = null
        } else if (streamSettings.security == Constants.REALITY) {
            streamSettings.tlsSettings = null
            streamSettings.realitySettings = tlsSetting
        }
    }

    private fun resolveHostToIP(host: String): List<String>? {
        try {
            if (Utils.isPureIpAddress(host)) {
                return null
            }

            val addresses = InetAddress.getAllByName(host)
            if (addresses.isEmpty()) {
                return null
            }

            val sortedAddresses = addresses.sortedWith(compareBy { it is Inet6Address })
            val ipList = sortedAddresses.mapNotNull { it.hostAddress }
            return ipList
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to resolve host to IP", e)
            return null
        }
    }

    private fun toJsonPretty(src: Any?): String? {
        if (src == null)
            return null
        val gsonPre = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter( // custom serializer is needed here since JSON by default parse number as Double, core will fail to start
                object : TypeToken<Double>() {}.type,
                JsonSerializer { src: Double?, _: Type?, _: JsonSerializationContext? ->
                    JsonPrimitive(
                        src?.toInt()
                    )
                }
            )
            .create()
        return gsonPre.toJson(src)
    }

    private fun parseString(src: String?): JsonObject? {
        if (src == null)
            return null
        try {
            return JsonParser.parseString(src).getAsJsonObject()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to parse JSON string", e)
            return null
        }
    }

    private fun readTextFromAssets(context: Context?): String {
        if (context == null) return ""

        return try {
            context.assets.open(EMPTY_CONFIG).use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to read asset file: $EMPTY_CONFIG", e)
            ""
        }
    }

    companion object {
        private const val STEALTH_HTTP_HEADERS_JSON =
            """{"version":"1.1","method":"GET","headers":{"userAgent":["Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.122 Mobile Safari/537.36"],"Accept":["text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"],"acceptEncoding":["gzip, deflate, br"],"Accept-Language":["ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7"],"Connection":["keep-alive"],"Upgrade-Insecure-Requests":["1"],"Sec-Fetch-Site":["none"],"Sec-Fetch-Mode":["navigate"],"Sec-Fetch-User":["?1"],"Sec-Fetch-Dest":["document"],"Pragma":["no-cache"],"Cache-Control":["no-cache"]}}"""
        private const val LOG_LEVEL = "warning"
        private const val EMPTY_CONFIG = "xRay_config.json"
        private const val LOOPBACK = "127.0.0.1"
        private const val DNS_PROXY = "1.1.1.1"
        private const val HEADER_TYPE_HTTP = "http"

        /**
         * DNS service domains for popular public DNS providers and specialized endpoints.
         *
         * These constants represent DNS-over-HTTPS (DoH), DNS-over-TLS (DoT), or other DNS service domains
         * required by the application to configure upstream DNS servers or remap domains.
         */
        private const val GOOGLEAPIS_CN_DOMAIN = "domain:googleapis.cn"
        private const val GOOGLEAPIS_COM_DOMAIN = "googleapis.com"
        private const val DNS_DNSPOD_DOMAIN = "dot.pub"
        private const val DNS_ALIDNS_DOMAIN = "dns.alidns.com"
        private const val DNS_CLOUDFLARE_DOMAIN = "one.one.one.one"
        private const val DNS_GOOGLE_DOMAIN = "dns.google"
        private const val DNS_QUAD9_DOMAIN = "dns.quad9.net"
        private const val DNS_YANDEX_DOMAIN = "common.dot.dns.yandex.net"

        /**
         * Lists of public DNS resolver IP addresses (IPv4/IPv6) for each provider.
         *
         * These address lists are used to configure upstream DNS resolvers within the application
         * and provide direct IP options for various providers (AliDNS, Cloudflare, Google, DNSPod, Quad9, Yandex).
         * IPv6 addresses are included when available.
         */
        private val DNS_ALIDNS_ADDRESSES =
            listOf("223.5.5.5", "223.6.6.6", "2400:3200::1", "2400:3200:baba::1")
        private val DNS_CLOUDFLARE_ADDRESSES =
            listOf("1.1.1.1", "1.0.0.1", "2606:4700:4700::1111", "2606:4700:4700::1001")
        private val DNS_DNSPOD_ADDRESSES = listOf("1.12.12.12", "120.53.53.53")
        private val DNS_GOOGLE_ADDRESSES =
            listOf("8.8.8.8", "8.8.4.4", "2001:4860:4860::8888", "2001:4860:4860::8844")
        private val DNS_QUAD9_ADDRESSES =
            listOf("9.9.9.9", "149.112.112.112", "2620:fe::fe", "2620:fe::9")
        private val DNS_YANDEX_ADDRESSES =
            listOf("77.88.8.8", "77.88.8.1", "2a02:6b8::feed:0ff", "2a02:6b8:0:1::feed:0ff")
    }
}
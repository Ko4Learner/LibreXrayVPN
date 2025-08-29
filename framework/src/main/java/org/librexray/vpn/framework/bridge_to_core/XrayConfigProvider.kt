/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.framework.bridge_to_core

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
import org.librexray.vpn.core.utils.Constants
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigResult
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.domain.models.NetworkType
import org.librexray.vpn.framework.models.XrayConfig
import org.librexray.vpn.framework.outbound_converter.HttpConverter
import org.librexray.vpn.framework.outbound_converter.ShadowsocksConverter
import org.librexray.vpn.framework.outbound_converter.SocksConverter
import org.librexray.vpn.framework.outbound_converter.TrojanConverter
import org.librexray.vpn.framework.outbound_converter.VlessConverter
import org.librexray.vpn.framework.outbound_converter.VmessConverter
import org.librexray.vpn.framework.outbound_converter.WireguardConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Type
import java.net.Inet6Address
import java.net.InetAddress
import javax.inject.Inject

/**
 * Builds Xray core configuration from a base template and a protocol profile.
 */
class XrayConfigProvider @Inject constructor(
    private val storage: KeyValueStorage,
    private val gson: Gson,
    private val httpConverter: HttpConverter,
    private val shadowsocksConverter: ShadowsocksConverter,
    private val socksConverter: SocksConverter,
    private val trojanConverter: TrojanConverter,
    private val vlessConverter: VlessConverter,
    private val vmessConverter: VmessConverter,
    private val wireguardConverter: WireguardConverter,
    @ApplicationContext private val context: Context
) {
    private var initConfigCache: String? = null

    /**
     * Returns a composed Xray configuration for the given profile GUID.
     *
     * @return [ConfigResult] where `status=true` and `content` is a JSON string on success.
     *         If the profile is missing or invalid, returns `status=false`.
     */
    fun getCoreConfig(guid: String): ConfigResult {
        try {
            val config = storage.decodeServerConfig(guid) ?: return ConfigResult(false)
            return getXrayNormalConfig(context, guid, config)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get Xray config", e)
            return ConfigResult(false)
        }
    }

    /**
     * Composes a full config by validating the profile, updating base template and
     * wiring inbounds/outbounds, DNS and routing.
     */
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
        xRayConfig.routing.domainStrategy = IP_IF_NON_MATCH

        getInbounds(xRayConfig)

        if (!getOutbounds(xRayConfig, config)) return result

        getDns(xRayConfig)

        resolveOutboundDomainsToHosts(xRayConfig)

        result.status = true
        result.content = toJsonPretty(xRayConfig) ?: ""
        result.guid = guid
        return result
    }

    /**
     * Loads the base Xray JSON template from assets (cached after first read).
     */
    private fun initXrayConfig(context: Context): XrayConfig? {
        val assets = initConfigCache ?: readTextFromAssets(context)
        if (TextUtils.isEmpty(assets)) {
            return null
        }
        initConfigCache = assets
        val config = gson.fromJson(assets, XrayConfig::class.java)
        return config
    }

    /**
     * Configures inbound listeners (loopback, ports, sniffing) on the template.
     */
    private fun getInbounds(xrayConfig: XrayConfig): Boolean {
        try {
            xrayConfig.inbounds.forEach { curInbound ->
                curInbound.listen = LOOPBACK
            }
            xrayConfig.inbounds[0].port = LOCAL_SOCKS_PORT
            xrayConfig.inbounds[0].sniffing?.enabled = true
            xrayConfig.inbounds[0].sniffing?.routeOnly = false
            xrayConfig.inbounds.removeAt(1)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure inbounds", e)
            return false
        }
        return true
    }

    /**
     * Builds an outbound from a profile and injects it into the template.
     */
    private fun getOutbounds(xrayConfig: XrayConfig, config: ConfigProfileItem): Boolean {
        val outbound = convertProfiletoOutbound(config) ?: return false
        val ret = updateOutboundWithGlobalSettings(outbound)
        if (!ret) return false

        if (xrayConfig.outbounds.isNotEmpty()) {
            xrayConfig.outbounds[0] = outbound
        } else {
            xrayConfig.outbounds.add(outbound)
        }
        return true
    }

    /**
     * Applies DNS servers and host remappings to the template.
     */
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

    /**
     * Applies global adjustments to the outbound (e.g., mux, HTTP header stealth, WireGuard address).
     */
    private fun updateOutboundWithGlobalSettings(outbound: XrayConfig.OutboundBean): Boolean {
        try {
            val protocol = outbound.protocol
            outbound.mux?.enabled = false
            outbound.mux?.concurrency = -1

            if (protocol.equals(ConfigType.WIREGUARD.name, true)) {
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

    /**
     * Resolves outbound target domains to IP addresses and writes them into DNS hosts.
     * Also sets domain strategy to prefer both IPv4 and IPv6 where applicable.
     */
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

            item.ensureSockopt().domainStrategy = USE_IPV4V6
            newHosts[domain] = if (resolvedIps.size == 1) {
                resolvedIps[0]
            } else {
                resolvedIps
            }
        }

        dns.hosts = newHosts
    }

    /**
     * Converts a profile into an outbound according to its protocol type.
     */
    private fun convertProfiletoOutbound(profileItem: ConfigProfileItem): XrayConfig.OutboundBean? {
        return when (profileItem.configType) {
            ConfigType.VMESS -> vmessConverter.toOutbound(profileItem)
            ConfigType.SHADOWSOCKS -> shadowsocksConverter.toOutbound(profileItem)
            ConfigType.SOCKS -> socksConverter.toOutbound(profileItem)
            ConfigType.VLESS -> vlessConverter.toOutbound(profileItem)
            ConfigType.TROJAN -> trojanConverter.toOutbound(profileItem)
            ConfigType.WIREGUARD -> wireguardConverter.toOutbound(profileItem)
            ConfigType.HTTP -> httpConverter.toOutbound(profileItem)
        }
    }

    /**
     * Creates a minimal outbound skeleton for the given protocol type, used for initial drafts.
     */
    fun createInitOutbound(configType: ConfigType): XrayConfig.OutboundBean? {
        return when (configType) {
            ConfigType.VMESS, ConfigType.VLESS -> XrayConfig.OutboundBean(
                protocol = configType.name.lowercase(),
                settings = XrayConfig.OutboundBean.OutSettingsBean(
                    vnext = listOf(
                        XrayConfig.OutboundBean.OutSettingsBean.VnextBean(
                            users = listOf(XrayConfig.OutboundBean.OutSettingsBean.VnextBean.UsersBean())
                        )
                    )
                ), streamSettings = XrayConfig.OutboundBean.StreamSettingsBean()
            )

            ConfigType.SHADOWSOCKS,
            ConfigType.SOCKS,
            ConfigType.HTTP,
            ConfigType.TROJAN -> XrayConfig.OutboundBean(
                protocol = configType.name.lowercase(),
                settings = XrayConfig.OutboundBean.OutSettingsBean(
                    servers = listOf(XrayConfig.OutboundBean.OutSettingsBean.ServersBean())
                ),
                streamSettings = XrayConfig.OutboundBean.StreamSettingsBean()
            )

            ConfigType.WIREGUARD -> XrayConfig.OutboundBean(
                protocol = configType.name.lowercase(),
                settings = XrayConfig.OutboundBean.OutSettingsBean(
                    secretKey = "",
                    peers = listOf(XrayConfig.OutboundBean.OutSettingsBean.WireGuardBean())
                )
            )
        }
    }

    /**
     * Fills transport-specific stream settings (TCP/WS/GRPC/etc) from a profile.
     *
     * @return The inferred SNI (if any) to be used later in TLS/REALITY settings.
     */
    fun populateTransportSettings(
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
                    tcpSetting.header.type = NONE
                    sni = host
                }
                streamSettings.tcpSettings = tcpSetting
            }

            NetworkType.KCP.type -> {
                val kcpsetting = XrayConfig.OutboundBean.StreamSettingsBean.KcpSettingsBean()
                kcpsetting.header.type = headerType ?: NONE
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
                grpcSetting.multiMode = mode == MULTI
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

    /**
     * Applies TLS or REALITY settings onto the stream based on profile values.
     *
     * @param sniExt SNI suggested by transport (e.g., first Host), used if profile SNI is empty.
     */
    fun populateTlsSettings(
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

    /**
     * Resolves a domain to a list of IP addresses (IPv4 first, then IPv6).
     * Returns null if input is already an IP or resolution fails.
     */
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

    /**
     * Serializes an object to JSON. Doubles are coerced to Ints where required.
     */
    private fun toJsonPretty(src: Any?): String? {
        if (src == null)
            return null
        val gsonPre = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(
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

    /**
     * Safely parses a JSON string into [JsonObject], returning null on failure.
     */
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

    /**
     * Reads the base config template from the app assets folder.
     */
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
        private const val LOCAL_SOCKS_PORT = 10808
        private const val DNS_PROXY = "1.1.1.1"
        private const val HEADER_TYPE_HTTP = "http"
        private const val IP_IF_NON_MATCH = "IPIfNonMatch"
        private const val USE_IPV4V6 = "UseIPv4v6"
        private const val NONE = "none"
        private const val MULTI = "multi"

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
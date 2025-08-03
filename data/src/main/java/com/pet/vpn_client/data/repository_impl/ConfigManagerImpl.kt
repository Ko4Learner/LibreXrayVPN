package com.pet.vpn_client.data.repository_impl

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.XrayConfig
import com.pet.vpn_client.data.config_formatter.HttpFormatter
import com.pet.vpn_client.data.config_formatter.ShadowsocksFormatter
import com.pet.vpn_client.data.config_formatter.SocksFormatter
import com.pet.vpn_client.data.config_formatter.TrojanFormatter
import com.pet.vpn_client.data.config_formatter.VlessFormatter
import com.pet.vpn_client.data.config_formatter.VmessFormatter
import com.pet.vpn_client.data.config_formatter.WireguardFormatter
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.HttpUtil
import com.pet.vpn_client.core.utils.JsonUtil
import com.pet.vpn_client.core.utils.Utils
import javax.inject.Inject

class ConfigManagerImpl @Inject constructor(
    val storage: KeyValueStorage,
    val gson: Gson,
    val httpFormatter: HttpFormatter,
    val shadowsocksFormatter: ShadowsocksFormatter,
    val socksFormatter: SocksFormatter,
    val trojanFormatter: TrojanFormatter,
    val vlessFormatter: VlessFormatter,
    val vmessFormatter: VmessFormatter,
    val wireguardFormatter: WireguardFormatter,
    val context: Context
) : ConfigManager {

    private var initConfigCache: String? = null

    override fun getCoreConfig(guid: String): ConfigResult {
        try {
            val config = storage.decodeServerConfig(guid) ?: return ConfigResult(false)
            return getV2rayNormalConfig(context, guid, config)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get V2ray config", e)
            return ConfigResult(false)
        }
    }

    private fun getV2rayNormalConfig(
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

        val xRayConfig = initV2rayConfig(context) ?: return result
        xRayConfig.log.loglevel = "warning"
        xRayConfig.remarks = config.remarks
        xRayConfig.routing.domainStrategy = "IPIfNonMatch"
        xRayConfig.stats = null
        xRayConfig.policy = null

        getInbounds(xRayConfig)

        getOutbounds(xRayConfig, config) ?: return result

        getDns(xRayConfig)



        resolveOutboundDomainsToHosts(xRayConfig)

        result.status = true
        result.content = JsonUtil.toJsonPretty(xRayConfig) ?: ""
        result.guid = guid
        return result
    }

    private fun initV2rayConfig(context: Context): XrayConfig? {
        val assets = initConfigCache ?: Utils.readTextFromAssets(context, "v2ray_config.json")
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
                curInbound.listen = Constants.LOOPBACK
            }
            xrayConfig.inbounds[0].port = 10808
            val fakedns = false
            val sniffAllTlsAndHttp = true
            xrayConfig.inbounds[0].sniffing?.enabled = fakedns || sniffAllTlsAndHttp
            xrayConfig.inbounds[0].sniffing?.routeOnly = false
            if (!sniffAllTlsAndHttp) {
                xrayConfig.inbounds[0].sniffing?.destOverride?.clear()
            }
            if (fakedns) {
                xrayConfig.inbounds[0].sniffing?.destOverride?.add("fakedns")
            }
            xrayConfig.inbounds.removeAt(1)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure inbounds", e)
            return false
        }
        return true
    }

    private fun getDns(xrayConfig: XrayConfig): Boolean {
        try {
            val hosts = mutableMapOf<String, Any>()
            val servers = ArrayList<Any>()

            //remote Dns
            val remoteDns = listOf(Constants.DNS_PROXY)
            remoteDns.forEach {
                servers.add(it)
            }

            // TODO необходимость?
            // domestic DNS
            val domesticDns = listOf(Constants.DNS_DIRECT)
            if (Utils.isPureIpAddress(domesticDns.first())) {
                xrayConfig.routing.rules.add(
                    0, XrayConfig.RoutingBean.RulesBean(
                        outboundTag = Constants.TAG_DIRECT,
                        port = "53",
                        ip = arrayListOf(domesticDns.first()),
                        domain = null
                    )
                )
            }


            // hardcode googleapi rule to fix play store problems
            hosts[Constants.GOOGLEAPIS_CN_DOMAIN] = Constants.GOOGLEAPIS_COM_DOMAIN

            // hardcode popular Android Private DNS rule to fix localhost DNS problem
            hosts[Constants.DNS_ALIDNS_DOMAIN] = Constants.DNS_ALIDNS_ADDRESSES
            hosts[Constants.DNS_CLOUDFLARE_DOMAIN] = Constants.DNS_CLOUDFLARE_ADDRESSES
            hosts[Constants.DNS_DNSPOD_DOMAIN] = Constants.DNS_DNSPOD_ADDRESSES
            hosts[Constants.DNS_GOOGLE_DOMAIN] = Constants.DNS_GOOGLE_ADDRESSES
            hosts[Constants.DNS_QUAD9_DOMAIN] = Constants.DNS_QUAD9_ADDRESSES
            hosts[Constants.DNS_YANDEX_DOMAIN] = Constants.DNS_YANDEX_ADDRESSES

            // DNS dns
            xrayConfig.dns = XrayConfig.DnsBean(
                servers = servers,
                hosts = hosts
            )

            // DNS routing
            if (Utils.isPureIpAddress(remoteDns.first())) {
                xrayConfig.routing.rules.add(
                    0, XrayConfig.RoutingBean.RulesBean(
                        outboundTag = Constants.TAG_PROXY,
                        port = "53",
                        ip = arrayListOf(remoteDns.first()),
                        domain = null
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure DNS", e)
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

    private fun updateOutboundWithGlobalSettings(outbound: XrayConfig.OutboundBean): Boolean {
        try {
            val protocol = outbound.protocol
            outbound.mux?.enabled = false
            outbound.mux?.concurrency = -1

            if (protocol.equals(EConfigType.WIREGUARD.name, true)) {
                var localTunAddr = if (outbound.settings?.address == null) {
                    listOf(Constants.WIREGUARD_LOCAL_ADDRESS_V4)
                } else {
                    outbound.settings?.address as List<*>
                }
                localTunAddr = listOf(localTunAddr.first())
                outbound.settings?.address = localTunAddr
            }

            if (outbound.streamSettings?.network == Constants.DEFAULT_NETWORK
                && outbound.streamSettings?.tcpSettings?.header?.type == Constants.HEADER_TYPE_HTTP
            ) {
                val path = outbound.streamSettings?.tcpSettings?.header?.request?.path
                val host = outbound.streamSettings?.tcpSettings?.header?.request?.headers?.Host

                val requestString: String by lazy {
                    """{"version":"1.1","method":"GET","headers":{"User-Agent":["Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.122 Mobile Safari/537.36"],"Accept-Encoding":["gzip, deflate"],"Connection":["keep-alive"],"Pragma":"no-cache"}}"""
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

            val resolvedIps = HttpUtil.resolveHostToIP(domain, false)
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
        streamSettings.network = if (transport.isEmpty()) NetworkType.TCP.type else transport
        when (streamSettings.network) {
            NetworkType.TCP.type -> {
                val tcpSetting = XrayConfig.OutboundBean.StreamSettingsBean.TcpSettingsBean()
                if (headerType == Constants.HEADER_TYPE_HTTP) {
                    tcpSetting.header.type = Constants.HEADER_TYPE_HTTP
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
                xhttpSetting.extra = JsonUtil.parseString(xhttpExtra)
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

        streamSettings.security = if (streamSecurity.isEmpty()) null else streamSecurity
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
}
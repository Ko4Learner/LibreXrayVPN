package com.pet.vpn_client.data

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.data.dto.XrayConfig
import com.pet.vpn_client.data.dto.XrayConfig.OutboundBean
import com.pet.vpn_client.data.dto.XrayConfig.OutboundBean.OutSettingsBean
import com.pet.vpn_client.data.dto.XrayConfig.OutboundBean.StreamSettingsBean
import com.pet.vpn_client.data.dto.XrayConfig.RoutingBean.RulesBean
import com.pet.vpn_client.data.config_formatter.HttpFormatter
import com.pet.vpn_client.data.config_formatter.ShadowsocksFormatter
import com.pet.vpn_client.data.config_formatter.SocksFormatter
import com.pet.vpn_client.data.config_formatter.TrojanFormatter
import com.pet.vpn_client.data.config_formatter.VlessFormatter
import com.pet.vpn_client.data.config_formatter.VmessFormatter
import com.pet.vpn_client.data.config_formatter.WireguardFormatter
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.domain.models.RulesetItem
import com.pet.vpn_client.utils.HttpUtil
import com.pet.vpn_client.utils.JsonUtil
import com.pet.vpn_client.utils.Utils
import com.pet.vpn_client.utils.isNotNullEmpty
import javax.inject.Inject

class ConfigManager @Inject constructor(
    val storage: KeyValueStorage,
    val gson: Gson,
    val settingsManager: SettingsManager,
    val httpFormatter: HttpFormatter,
    val shadowsocksFormatter: ShadowsocksFormatter,
    val socksFormatter: SocksFormatter,
    val trojanFormatter: TrojanFormatter,
    val vlessFormatter: VlessFormatter,
    val vmessFormatter: VmessFormatter,
    val wireguardFormatter: WireguardFormatter
) {

    private var initConfigCache: String? = null

    //region get config function

    fun getV2rayConfig(context: Context, guid: String): ConfigResult {
        try {
            val config = storage.decodeServerConfig(guid) ?: return ConfigResult(false)
            return getV2rayNormalConfig(context, guid, config)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get V2ray config", e)
            return ConfigResult(false)
        }
    }

    fun getV2rayConfig4Speedtest(context: Context, guid: String): ConfigResult {
        try {
            val config = storage.decodeServerConfig(guid) ?: return ConfigResult(false)
            return getV2rayNormalConfig4Speedtest(context, guid, config)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get V2ray config for speedtest", e)
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

        val v2rayConfig = initV2rayConfig(context) ?: return result
        v2rayConfig.log.loglevel =
            storage.decodeSettingsString(Constants.PREF_LOGLEVEL) ?: "warning"
        v2rayConfig.remarks = config.remarks

        getInbounds(v2rayConfig)

        getOutbounds(v2rayConfig, config) ?: return result
        getMoreOutbounds(v2rayConfig, config.subscriptionId)

        getRouting(v2rayConfig)

        getFakeDns(v2rayConfig)

        getDns(v2rayConfig)

        if (storage.decodeSettingsBool(Constants.PREF_LOCAL_DNS_ENABLED) == true) {
            getCustomLocalDns(v2rayConfig)
        }
        if (storage.decodeSettingsBool(Constants.PREF_SPEED_ENABLED) != true) {
            v2rayConfig.stats = null
            v2rayConfig.policy = null
        }

        resolveOutboundDomainsToHosts(v2rayConfig)

        result.status = true
        result.content = JsonUtil.toJsonPretty(v2rayConfig) ?: ""
        result.guid = guid
        return result
    }

    private fun getV2rayNormalConfig4Speedtest(
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

        val v2rayConfig = initV2rayConfig(context) ?: return result

        getOutbounds(v2rayConfig, config) ?: return result
        getMoreOutbounds(v2rayConfig, config.subscriptionId)

        v2rayConfig.log.loglevel =
            storage.decodeSettingsString(Constants.PREF_LOGLEVEL) ?: "warning"
        v2rayConfig.inbounds.clear()
        v2rayConfig.routing.rules.clear()
        v2rayConfig.dns = null
        v2rayConfig.fakedns = null
        v2rayConfig.stats = null
        v2rayConfig.policy = null

        v2rayConfig.outbounds.forEach { key ->
            key.mux = null
        }

        result.status = true
        result.content = JsonUtil.toJsonPretty(v2rayConfig) ?: ""
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


    //endregion


    //region some sub function

    private fun getInbounds(xrayConfig: XrayConfig): Boolean {
        try {
            val socksPort = settingsManager.getSocksPort()

            xrayConfig.inbounds.forEach { curInbound ->
                if (storage.decodeSettingsBool(Constants.PREF_PROXY_SHARING) != true) {
                    //bind all inbounds to localhost if the user requests
                    curInbound.listen = Constants.LOOPBACK
                }
            }
            xrayConfig.inbounds[0].port = socksPort
            val fakedns = storage.decodeSettingsBool(Constants.PREF_FAKE_DNS_ENABLED) == true
            val sniffAllTlsAndHttp =
                storage.decodeSettingsBool(Constants.PREF_SNIFFING_ENABLED, true) != false
            xrayConfig.inbounds[0].sniffing?.enabled = fakedns || sniffAllTlsAndHttp
            xrayConfig.inbounds[0].sniffing?.routeOnly =
                storage.decodeSettingsBool(Constants.PREF_ROUTE_ONLY_ENABLED, false)
            if (!sniffAllTlsAndHttp) {
                xrayConfig.inbounds[0].sniffing?.destOverride?.clear()
            }
            if (fakedns) {
                xrayConfig.inbounds[0].sniffing?.destOverride?.add("fakedns")
            }

            //if (Utils.isXray()) {
            xrayConfig.inbounds.removeAt(1)
            //} else {
            //    val httpPort = settingsManager.getHttpPort()
            //    xrayConfig.inbounds[1].port = httpPort
            //}

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure inbounds", e)
            return false
        }
        return true
    }

    private fun getFakeDns(xrayConfig: XrayConfig) {
        if (storage.decodeSettingsBool(Constants.PREF_LOCAL_DNS_ENABLED) == true
            && storage.decodeSettingsBool(Constants.PREF_FAKE_DNS_ENABLED) == true
        ) {
            xrayConfig.fakedns = listOf(XrayConfig.FakednsBean())
        }
    }

    private fun getRouting(xrayConfig: XrayConfig): Boolean {
        try {

            xrayConfig.routing.domainStrategy =
                storage.decodeSettingsString(Constants.PREF_ROUTING_DOMAIN_STRATEGY)
                    ?: "IPIfNonMatch"

            val rulesetItems = storage.decodeRoutingRulesets()
            rulesetItems?.forEach { key ->
                getRoutingUserRule(key, xrayConfig)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure routing", e)
            return false
        }
        return true
    }

    private fun getRoutingUserRule(item: RulesetItem?, xrayConfig: XrayConfig) {
        try {
            if (item == null || !item.enabled) {
                return
            }

            val rule = gson.fromJson(gson.toJson(item), RulesBean::class.java) ?: return

            xrayConfig.routing.rules.add(rule)

        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to apply routing user rule", e)
        }
    }

    private fun getUserRule2Domain(tag: String): ArrayList<String> {
        val domain = ArrayList<String>()

        val rulesetItems = storage.decodeRoutingRulesets()
        rulesetItems?.forEach { key ->
            if (key.enabled && key.outboundTag == tag && !key.domain.isNullOrEmpty()) {
                key.domain?.forEach {
                    if (it != Constants.GEOSITE_PRIVATE
                        && (it.startsWith("geosite:") || it.startsWith("domain:"))
                    ) {
                        domain.add(it)
                    }
                }
            }
        }

        return domain
    }

    private fun getCustomLocalDns(xrayConfig: XrayConfig): Boolean {
        try {
            if (storage.decodeSettingsBool(Constants.PREF_FAKE_DNS_ENABLED) == true) {
                val geositeCn = arrayListOf(Constants.GEOSITE_CN)
                val proxyDomain = getUserRule2Domain(Constants.TAG_PROXY)
                val directDomain = getUserRule2Domain(Constants.TAG_DIRECT)
                xrayConfig.dns?.servers?.add(
                    0,
                    XrayConfig.DnsBean.ServersBean(
                        address = "fakedns",
                        domains = geositeCn.plus(proxyDomain).plus(directDomain)
                    )
                )
            }

            // DNS inbound
            val remoteDns = settingsManager.getRemoteDnsServers()
            if (xrayConfig.inbounds.none { e -> e.protocol == "dokodemo-door" && e.tag == "dns-in" }) {
                val dnsInboundSettings = XrayConfig.InboundBean.InSettingsBean(
                    address = if (Utils.isPureIpAddress(remoteDns.first())) remoteDns.first() else Constants.DNS_PROXY,
                    port = 53,
                    network = "tcp,udp"
                )

                val localDnsPort = Utils.parseInt(
                    storage.decodeSettingsString(Constants.PREF_LOCAL_DNS_PORT),
                    Constants.PORT_LOCAL_DNS.toInt()
                )
                xrayConfig.inbounds.add(
                    XrayConfig.InboundBean(
                        tag = "dns-in",
                        port = localDnsPort,
                        listen = Constants.LOOPBACK,
                        protocol = "dokodemo-door",
                        settings = dnsInboundSettings,
                        sniffing = null
                    )
                )
            }

            // DNS outbound
            if (xrayConfig.outbounds.none { e -> e.protocol == "dns" && e.tag == "dns-out" }) {
                xrayConfig.outbounds.add(
                    OutboundBean(
                        protocol = "dns",
                        tag = "dns-out",
                        settings = null,
                        streamSettings = null,
                        mux = null
                    )
                )
            }

            // DNS routing tag
            xrayConfig.routing.rules.add(
                0, RulesBean(
                    inboundTag = arrayListOf("dns-in"),
                    outboundTag = "dns-out",
                    domain = null
                )
            )
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure custom local DNS", e)
            return false
        }
        return true
    }

    private fun getDns(xrayConfig: XrayConfig): Boolean {
        try {
            val hosts = mutableMapOf<String, Any>()
            val servers = ArrayList<Any>()

            //remote Dns
            val remoteDns = settingsManager.getRemoteDnsServers()
            val proxyDomain = getUserRule2Domain(Constants.TAG_PROXY)
            remoteDns.forEach {
                servers.add(it)
            }
            if (proxyDomain.isNotEmpty()) {
                servers.add(
                    XrayConfig.DnsBean.ServersBean(
                        address = remoteDns.first(),
                        domains = proxyDomain,
                    )
                )
            }

            // domestic DNS
            val domesticDns = settingsManager.getDomesticDnsServers()
            val directDomain = getUserRule2Domain(Constants.TAG_DIRECT)
            val isCnRoutingMode = directDomain.contains(Constants.GEOSITE_CN)
            val geoipCn = arrayListOf(Constants.GEOIP_CN)
            if (directDomain.isNotEmpty()) {
                servers.add(
                    XrayConfig.DnsBean.ServersBean(
                        address = domesticDns.first(),
                        domains = directDomain,
                        expectIPs = if (isCnRoutingMode) geoipCn else null,
                        skipFallback = true
                    )
                )
            }

            if (Utils.isPureIpAddress(domesticDns.first())) {
                xrayConfig.routing.rules.add(
                    0, RulesBean(
                        outboundTag = Constants.TAG_DIRECT,
                        port = "53",
                        ip = arrayListOf(domesticDns.first()),
                        domain = null
                    )
                )
            }

            //User DNS hosts
            try {
                val userHosts = storage.decodeSettingsString(Constants.PREF_DNS_HOSTS)
                if (userHosts.isNotNullEmpty()) {
                    var userHostsMap = userHosts?.split(",")
                        ?.filter { it.isNotEmpty() }
                        ?.filter { it.contains(":") }
                        ?.associate { it.split(":").let { (k, v) -> k to v } }
                    if (userHostsMap != null) hosts.putAll(userHostsMap)
                }
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to configure user DNS hosts", e)
            }

            //block dns
            val blkDomain = getUserRule2Domain(Constants.TAG_BLOCKED)
            if (blkDomain.isNotEmpty()) {
                hosts.putAll(blkDomain.map { it to Constants.LOOPBACK })
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
                    0, RulesBean(
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


    //endregion


    //region outbound related functions

    private fun getOutbounds(xrayConfig: XrayConfig, config: ConfigProfileItem): Boolean? {
        val outbound = convertProfile2Outbound(config) ?: return null
        val ret = updateOutboundWithGlobalSettings(outbound)
        if (!ret) return null

        if (xrayConfig.outbounds.isNotEmpty()) {
            xrayConfig.outbounds[0] = outbound
        } else {
            xrayConfig.outbounds.add(outbound)
        }

        updateOutboundFragment(xrayConfig)
        return true
    }

    private fun getPlusOutbounds(xrayConfig: XrayConfig): Int? {
        try {
            val socksPort = Utils.findFreePort(listOf(100 + settingsManager.getSocksPort(), 0))

            val outboundNew = OutboundBean(
                mux = null,
                protocol = EConfigType.SOCKS.name.lowercase(),
                settings = OutSettingsBean(
                    servers = listOf(
                        OutSettingsBean.ServersBean(
                            address = Constants.LOOPBACK,
                            port = socksPort
                        )
                    )
                )
            )
            if (xrayConfig.outbounds.isNotEmpty()) {
                xrayConfig.outbounds[0] = outboundNew
            } else {
                xrayConfig.outbounds.add(outboundNew)
            }

            return socksPort
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure plusOutbound", e)
            return null
        }
    }

    private fun getMoreOutbounds(xrayConfig: XrayConfig, subscriptionId: String): Boolean {
        //fragment proxy
        if (storage.decodeSettingsBool(Constants.PREF_FRAGMENT_ENABLED, false) == true) {
            return false
        }

        if (subscriptionId.isEmpty()) {
            return false
        }
        try {
            val subItem = storage.decodeSubscription(subscriptionId) ?: return false

            //current proxy
            val outbound = xrayConfig.outbounds[0]

            //Previous proxy
            val prevNode = settingsManager.getServerViaRemarks(subItem.prevProfile)
            if (prevNode != null) {
                val prevOutbound = convertProfile2Outbound(prevNode)
                if (prevOutbound != null) {
                    updateOutboundWithGlobalSettings(prevOutbound)
                    prevOutbound.tag = Constants.TAG_PROXY + "2"
                    xrayConfig.outbounds.add(prevOutbound)
                    outbound.ensureSockopt().dialerProxy = prevOutbound.tag
                }
            }

            //Next proxy
            val nextNode = settingsManager.getServerViaRemarks(subItem.nextProfile)
            if (nextNode != null) {
                val nextOutbound = convertProfile2Outbound(nextNode)
                if (nextOutbound != null) {
                    updateOutboundWithGlobalSettings(nextOutbound)
                    nextOutbound.tag = Constants.TAG_PROXY
                    xrayConfig.outbounds.add(0, nextOutbound)
                    outbound.tag = Constants.TAG_PROXY + "1"
                    nextOutbound.ensureSockopt().dialerProxy = outbound.tag
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to configure more outbounds", e)
            return false
        }

        return true
    }

    private fun updateOutboundWithGlobalSettings(outbound: OutboundBean): Boolean {
        try {
            var muxEnabled = storage.decodeSettingsBool(Constants.PREF_MUX_ENABLED, false)
            val protocol = outbound.protocol
            if (protocol.equals(EConfigType.SHADOWSOCKS.name, true)
                || protocol.equals(EConfigType.SOCKS.name, true)
                || protocol.equals(EConfigType.HTTP.name, true)
                || protocol.equals(EConfigType.TROJAN.name, true)
                || protocol.equals(EConfigType.WIREGUARD.name, true)
            ) {
                muxEnabled = false
            } else if (outbound.streamSettings?.network == NetworkType.XHTTP.type) {
                muxEnabled = false
            }

            if (muxEnabled == true) {
                outbound.mux?.enabled = true
                outbound.mux?.concurrency =
                    storage.decodeSettingsString(Constants.PREF_MUX_CONCURRENCY, "8").orEmpty()
                        .toInt()
                outbound.mux?.xudpConcurrency =
                    storage.decodeSettingsString(Constants.PREF_MUX_XUDP_CONCURRENCY, "16")
                        .orEmpty().toInt()
                outbound.mux?.xudpProxyUDP443 =
                    storage.decodeSettingsString(Constants.PREF_MUX_XUDP_QUIC, "reject")
                if (protocol.equals(
                        EConfigType.VLESS.name,
                        true
                    ) && outbound.settings?.vnext?.first()?.users?.first()?.flow?.isNotEmpty() == true
                ) {
                    outbound.mux?.concurrency = -1
                }
            } else {
                outbound.mux?.enabled = false
                outbound.mux?.concurrency = -1
            }

            if (protocol.equals(EConfigType.WIREGUARD.name, true)) {
                var localTunAddr = if (outbound.settings?.address == null) {
                    listOf(Constants.WIREGUARD_LOCAL_ADDRESS_V4)
                } else {
                    outbound.settings?.address as List<*>
                }
                if (storage.decodeSettingsBool(Constants.PREF_PREFER_IPV6) != true) {
                    localTunAddr = listOf(localTunAddr.first())
                }
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
                    StreamSettingsBean.TcpSettingsBean.HeaderBean.RequestBean::class.java
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

    private fun updateOutboundFragment(xrayConfig: XrayConfig): Boolean {
        try {
            if (storage.decodeSettingsBool(Constants.PREF_FRAGMENT_ENABLED, false) == false) {
                return true
            }
            if (xrayConfig.outbounds[0].streamSettings?.security != Constants.TLS
                && xrayConfig.outbounds[0].streamSettings?.security != Constants.REALITY
            ) {
                return true
            }

            val fragmentOutbound =
                OutboundBean(
                    protocol = Constants.PROTOCOL_FREEDOM,
                    tag = Constants.TAG_FRAGMENT,
                    mux = null
                )

            var packets =
                storage.decodeSettingsString(Constants.PREF_FRAGMENT_PACKETS) ?: "tlshello"
            if (xrayConfig.outbounds[0].streamSettings?.security == Constants.REALITY
                && packets == "tlshello"
            ) {
                packets = "1-3"
            } else if (xrayConfig.outbounds[0].streamSettings?.security == Constants.TLS
                && packets != "tlshello"
            ) {
                packets = "tlshello"
            }

            fragmentOutbound.settings = OutSettingsBean(
                fragment = OutSettingsBean.FragmentBean(
                    packets = packets,
                    length = storage.decodeSettingsString(Constants.PREF_FRAGMENT_LENGTH)
                        ?: "50-100",
                    interval = storage.decodeSettingsString(Constants.PREF_FRAGMENT_INTERVAL)
                        ?: "10-20"
                ),
                noises = listOf(
                    OutSettingsBean.NoiseBean(
                        type = "rand",
                        packet = "10-20",
                        delay = "10-16",
                    )
                ),
            )
            fragmentOutbound.streamSettings = StreamSettingsBean(
                sockopt = StreamSettingsBean.SockoptBean(
                    TcpNoDelay = true,
                    mark = 255
                )
            )
            xrayConfig.outbounds.add(fragmentOutbound)

            //proxy chain
            xrayConfig.outbounds[0].streamSettings?.sockopt =
                StreamSettingsBean.SockoptBean(
                    dialerProxy = Constants.TAG_FRAGMENT
                )
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to update outbound fragment", e)
            return false
        }
        return true
    }

    private fun resolveOutboundDomainsToHosts(xrayConfig: XrayConfig) {
        val proxyOutboundList = xrayConfig.getAllProxyOutbound()
        val dns = xrayConfig.dns ?: return
        val newHosts = dns.hosts?.toMutableMap() ?: mutableMapOf()
        val preferIpv6 = storage.decodeSettingsBool(Constants.PREF_PREFER_IPV6) == true

        for (item in proxyOutboundList) {
            val domain = item.getServerAddress()
            if (domain.isNullOrEmpty()) continue
            if (newHosts.containsKey(domain)) continue

            val resolvedIps = HttpUtil.resolveHostToIP(domain, preferIpv6)
            if (resolvedIps.isNullOrEmpty()) continue

            item.ensureSockopt().domainStrategy = if (preferIpv6) "UseIPv6v4" else "UseIPv4v6"
            newHosts[domain] = if (resolvedIps.size == 1) {
                resolvedIps[0]
            } else {
                resolvedIps
            }
        }

        dns.hosts = newHosts
    }

    private fun convertProfile2Outbound(profileItem: ConfigProfileItem): OutboundBean? {
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

    fun createInitOutbound(configType: EConfigType): OutboundBean? {
        return when (configType) {
            EConfigType.VMESS,
            EConfigType.VLESS ->
                return OutboundBean(
                    protocol = configType.name.lowercase(),
                    settings = OutSettingsBean(
                        vnext = listOf(
                            OutSettingsBean.VnextBean(
                                users = listOf(OutSettingsBean.VnextBean.UsersBean())
                            )
                        )
                    ),
                    streamSettings = StreamSettingsBean()
                )

            EConfigType.SHADOWSOCKS,
            EConfigType.SOCKS,
            EConfigType.HTTP,
            EConfigType.TROJAN ->
                return OutboundBean(
                    protocol = configType.name.lowercase(),
                    settings = OutSettingsBean(
                        servers = listOf(OutSettingsBean.ServersBean())
                    ),
                    streamSettings = StreamSettingsBean()
                )

            EConfigType.WIREGUARD ->
                return OutboundBean(
                    protocol = configType.name.lowercase(),
                    settings = OutSettingsBean(
                        secretKey = "",
                        peers = listOf(OutSettingsBean.WireGuardBean())
                    )
                )
        }
    }

    fun populateTransportSettings(
        streamSettings: StreamSettingsBean,
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
                val tcpSetting = StreamSettingsBean.TcpSettingsBean()
                if (headerType == Constants.HEADER_TYPE_HTTP) {
                    tcpSetting.header.type = Constants.HEADER_TYPE_HTTP
                    if (!TextUtils.isEmpty(host) || !TextUtils.isEmpty(path)) {
                        val requestObj = StreamSettingsBean.TcpSettingsBean.HeaderBean.RequestBean()
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
                val kcpsetting = StreamSettingsBean.KcpSettingsBean()
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
                val wssetting = StreamSettingsBean.WsSettingsBean()
                wssetting.headers.Host = host.orEmpty()
                sni = host
                wssetting.path = path ?: "/"
                streamSettings.wsSettings = wssetting
            }

            NetworkType.HTTP_UPGRADE.type -> {
                val httpupgradeSetting = StreamSettingsBean.HttpupgradeSettingsBean()
                httpupgradeSetting.host = host.orEmpty()
                sni = host
                httpupgradeSetting.path = path ?: "/"
                streamSettings.httpupgradeSettings = httpupgradeSetting
            }

            NetworkType.XHTTP.type -> {
                val xhttpSetting = StreamSettingsBean.XhttpSettingsBean()
                xhttpSetting.host = host.orEmpty()
                sni = host
                xhttpSetting.path = path ?: "/"
                xhttpSetting.mode = xhttpMode
                xhttpSetting.extra = JsonUtil.parseString(xhttpExtra)
                streamSettings.xhttpSettings = xhttpSetting
            }

            NetworkType.H2.type, NetworkType.HTTP.type -> {
                streamSettings.network = NetworkType.H2.type
                val h2Setting = StreamSettingsBean.HttpSettingsBean()
                h2Setting.host =
                    host.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                sni = h2Setting.host.getOrNull(0)
                h2Setting.path = path ?: "/"
                streamSettings.httpSettings = h2Setting
            }

            NetworkType.GRPC.type -> {
                val grpcSetting = StreamSettingsBean.GrpcSettingsBean()
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

    fun populateTlsSettings(
        streamSettings: StreamSettingsBean,
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
        val tlsSetting = StreamSettingsBean.TlsSettingsBean(
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

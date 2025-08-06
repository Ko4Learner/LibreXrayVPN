package com.pet.vpn_client.domain.models

import com.pet.vpn_client.core.utils.Constants

data class XrayConfig(
    var remarks: String? = null,
    var stats: Any? = null,
    val log: LogBean,
    var policy: PolicyBean? = null,
    val inbounds: ArrayList<InboundBean>,
    var outbounds: ArrayList<OutboundBean>,
    var dns: DnsBean? = null,
    val routing: RoutingBean,
    val api: Any? = null,
    val transport: Any? = null,
    val reverse: Any? = null,
    var fakedns: Any? = null,
    val browserForwarder: Any? = null,
    var observatory: Any? = null,
    var burstObservatory: Any? = null
) {
    data class LogBean(
        val access: String? = null,
        val error: String? = null,
        var loglevel: String? = null,
        val dnsLog: Boolean? = null
    )

    data class InboundBean(
        var tag: String,
        var port: Int,
        var protocol: String,
        var listen: String? = null,
        val settings: Any? = null,
        val sniffing: SniffingBean? = null,
        val streamSettings: Any? = null,
        val allocate: Any? = null
    ) {
        data class SniffingBean(
            var enabled: Boolean,
            val destOverride: ArrayList<String>,
            val metadataOnly: Boolean? = null,
            var routeOnly: Boolean? = null
        )
    }

    data class OutboundBean(
        var tag: String = PROXY,
        var protocol: String,
        var settings: OutSettingsBean? = null,
        var streamSettings: StreamSettingsBean? = null,
        val proxySettings: Any? = null,
        val sendThrough: String? = null,
        var mux: MuxBean? = MuxBean(false)
    ) {
        data class OutSettingsBean(
            var vnext: List<VnextBean>? = null,
            var fragment: FragmentBean? = null,
            var noises: List<NoiseBean>? = null,
            var servers: List<ServersBean>? = null,
            /*Blackhole*/
            var response: Response? = null,
            /*DNS*/
            val network: String? = null,
            var address: Any? = null,
            val port: Int? = null,
            /*Freedom*/
            var domainStrategy: String? = null,
            val redirect: String? = null,
            val userLevel: Int? = null,
            /*Loopback*/
            val inboundTag: String? = null,
            /*Wireguard*/
            var secretKey: String? = null,
            val peers: List<WireGuardBean>? = null,
            var reserved: List<Int>? = null,
            var mtu: Int? = null,
            var obfsPassword: String? = null,
        ) {

            data class VnextBean(
                var address: String = "",
                var port: Int = Constants.DEFAULT_PORT,
                var users: List<UsersBean>
            ) {

                data class UsersBean(
                    var id: String = "",
                    var alterId: Int? = null,
                    var security: String? = null,
                    var level: Int = Constants.DEFAULT_LEVEL,
                    var encryption: String? = null,
                    var flow: String? = null
                )
            }

            data class FragmentBean(
                var packets: String? = null,
                var length: String? = null,
                var interval: String? = null
            )

            data class NoiseBean(
                var type: String? = null,
                var packet: String? = null,
                var delay: String? = null
            )

            data class ServersBean(
                var address: String = "",
                var method: String? = null,
                var ota: Boolean = false,
                var password: String? = null,
                var port: Int = Constants.DEFAULT_PORT,
                var level: Int = Constants.DEFAULT_LEVEL,
                val email: String? = null,
                var flow: String? = null,
                val ivCheck: Boolean? = null,
                var users: List<SocksUsersBean>? = null
            ) {
                data class SocksUsersBean(
                    var user: String = "",
                    var pass: String = "",
                    var level: Int = Constants.DEFAULT_LEVEL
                )
            }

            data class Response(var type: String)

            data class WireGuardBean(
                var publicKey: String = "",
                var preSharedKey: String? = null,
                var endpoint: String = ""
            )
        }

        data class StreamSettingsBean(
            var network: String = Constants.DEFAULT_NETWORK,
            var security: String? = null,
            var tcpSettings: TcpSettingsBean? = null,
            var kcpSettings: KcpSettingsBean? = null,
            var wsSettings: WsSettingsBean? = null,
            var httpupgradeSettings: HttpupgradeSettingsBean? = null,
            var xhttpSettings: XhttpSettingsBean? = null,
            var httpSettings: HttpSettingsBean? = null,
            var tlsSettings: TlsSettingsBean? = null,
            var quicSettings: QuicSettingBean? = null,
            var realitySettings: TlsSettingsBean? = null,
            var grpcSettings: GrpcSettingsBean? = null,
            val dsSettings: Any? = null,
            var sockopt: SockoptBean? = null
        ) {

            data class TcpSettingsBean(
                var header: HeaderBean = HeaderBean(),
                val acceptProxyProtocol: Boolean? = null
            ) {
                data class HeaderBean(
                    var type: String = NONE,
                    var request: RequestBean? = null,
                    var response: Any? = null
                ) {
                    data class RequestBean(
                        var path: List<String> = ArrayList(),
                        var headers: HeadersBean = HeadersBean(),
                        val version: String? = null,
                        val method: String? = null
                    ) {
                        data class HeadersBean(
                            var Host: List<String>? = ArrayList(),
                            val userAgent: List<String>? = null,
                            val acceptEncoding: List<String>? = null,
                            val Connection: List<String>? = null,
                            val Pragma: String? = null
                        )
                    }
                }
            }

            data class KcpSettingsBean(
                var mtu: Int = 1350,
                var tti: Int = 50,
                var uplinkCapacity: Int = 12,
                var downlinkCapacity: Int = 100,
                var congestion: Boolean = false,
                var readBufferSize: Int = 1,
                var writeBufferSize: Int = 1,
                var header: HeaderBean = HeaderBean(),
                var seed: String? = null
            ) {
                data class HeaderBean(
                    var type: String = NONE,
                    var domain: String? = null
                )
            }

            data class WsSettingsBean(
                var path: String? = null,
                var headers: HeadersBean = HeadersBean(),
                val maxEarlyData: Int? = null,
                val useBrowserForwarding: Boolean? = null,
                val acceptProxyProtocol: Boolean? = null
            ) {
                data class HeadersBean(var Host: String = "")
            }

            data class HttpupgradeSettingsBean(
                var path: String? = null,
                var host: String? = null,
                val acceptProxyProtocol: Boolean? = null
            )

            data class XhttpSettingsBean(
                var path: String? = null,
                var host: String? = null,
                var mode: String? = null,
                var extra: Any? = null,
            )

            data class HttpSettingsBean(
                var host: List<String> = ArrayList(),
                var path: String? = null
            )

            data class SockoptBean(
                var TcpNoDelay: Boolean? = null,
                var tcpKeepAliveIdle: Int? = null,
                var tcpFastOpen: Boolean? = null,
                var tproxy: String? = null,
                var mark: Int? = null,
                var dialerProxy: String? = null,
                var domainStrategy: String? = null
            )

            data class TlsSettingsBean(
                var allowInsecure: Boolean = false,
                var serverName: String? = null,
                val alpn: List<String>? = null,
                val minVersion: String? = null,
                val maxVersion: String? = null,
                val preferServerCipherSuites: Boolean? = null,
                val cipherSuites: String? = null,
                val fingerprint: String? = null,
                val certificates: List<Any>? = null,
                val disableSystemRoot: Boolean? = null,
                val enableSessionResumption: Boolean? = null,
                // REALITY settings
                val show: Boolean = false,
                var publicKey: String? = null,
                var shortId: String? = null,
                var spiderX: String? = null
            )

            data class QuicSettingBean(
                var security: String = NONE,
                var key: String = "",
                var header: HeaderBean = HeaderBean()
            ) {
                data class HeaderBean(var type: String = NONE)
            }

            data class GrpcSettingsBean(
                var serviceName: String = "",
                var authority: String? = null,
                var multiMode: Boolean? = null,
                var idle_timeout: Int? = null,
                var health_check_timeout: Int? = null
            )
        }

        data class MuxBean(
            var enabled: Boolean,
            var concurrency: Int? = null,
            var xudpConcurrency: Int? = null,
            var xudpProxyUDP443: String? = null,
        )

        fun getServerAddress(): String? {
            if (protocol.equals(EConfigType.VMESS.name, true)
                || protocol.equals(EConfigType.VLESS.name, true)
            ) {
                return settings?.vnext?.first()?.address
            } else if (protocol.equals(EConfigType.SHADOWSOCKS.name, true)
                || protocol.equals(EConfigType.SOCKS.name, true)
                || protocol.equals(EConfigType.HTTP.name, true)
                || protocol.equals(EConfigType.TROJAN.name, true)
            ) {
                return settings?.servers?.first()?.address
            } else if (protocol.equals(EConfigType.WIREGUARD.name, true)) {
                return settings?.peers?.first()?.endpoint?.substringBeforeLast(":")
            }
            return null
        }

        fun ensureSockopt(): StreamSettingsBean.SockoptBean {
            val stream = streamSettings ?: StreamSettingsBean().also {
                streamSettings = it
            }

            val sockopt = stream.sockopt ?: StreamSettingsBean.SockoptBean().also {
                stream.sockopt = it
            }

            return sockopt
        }
    }

    data class DnsBean(
        var servers: ArrayList<Any>? = null,
        var hosts: Map<String, Any>? = null,
        val clientIp: String? = null,
        val disableCache: Boolean? = null,
        val queryStrategy: String? = null,
        val tag: String? = null
    )

    data class RoutingBean(
        var domainStrategy: String,
        var domainMatcher: String? = null,
        var rules: ArrayList<RulesBean>,
        val balancers: List<Any>? = null
    ) {

        data class RulesBean(
            var type: String = FIELD,
            var ip: ArrayList<String>? = null,
            var domain: ArrayList<String>? = null,
            var outboundTag: String = "",
            var balancerTag: String? = null,
            var port: String? = null,
            val sourcePort: String? = null,
            val network: String? = null,
            val source: List<String>? = null,
            val user: List<String>? = null,
            var inboundTag: List<String>? = null,
            val protocol: List<String>? = null,
            val attrs: String? = null,
            val domainMatcher: String? = null
        )
    }

    data class PolicyBean(
        var levels: Map<String, LevelBean>,
        var system: Any? = null
    ) {
        data class LevelBean(
            var handshake: Int? = null,
            var connIdle: Int? = null,
            var uplinkOnly: Int? = null,
            var downlinkOnly: Int? = null,
            val statsUserUplink: Boolean? = null,
            val statsUserDownlink: Boolean? = null,
            var bufferSize: Int? = null
        )
    }

    fun getAllProxyOutbound(): List<OutboundBean> {
        return outbounds.filter { outbound ->
            EConfigType.entries.any { it.name.equals(outbound.protocol, ignoreCase = true) }
        }
    }

    companion object {
        private const val PROXY = "proxy"
        private const val NONE = "none"
        private const val FIELD = "field"
    }
}
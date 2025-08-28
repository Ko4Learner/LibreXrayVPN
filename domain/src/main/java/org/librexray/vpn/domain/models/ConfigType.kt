package org.librexray.vpn.domain.models

import org.librexray.vpn.core.utils.Constants

/**
 * Enumerates supported VPN/proxy configuration types.
 */
enum class ConfigType(val value: Int, val protocolScheme: String) {
    VMESS(1, Constants.VMESS),
    SHADOWSOCKS(2, Constants.SHADOWSOCKS),
    SOCKS(3, Constants.SOCKS),
    VLESS(4, Constants.VLESS),
    TROJAN(5, Constants.TROJAN),
    WIREGUARD(6, Constants.WIREGUARD),
    HTTP(7, Constants.HTTP);
}
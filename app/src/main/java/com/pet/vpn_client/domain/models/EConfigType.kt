package com.pet.vpn_client.domain.models

import com.pet.vpn_client.app.Constants

enum class EConfigType(val value: Int, val protocolScheme: String) {
    VMESS(1, Constants.VMESS),
    SHADOWSOCKS(2, Constants.SHADOWSOCKS),
    SOCKS(3, Constants.SOCKS),
    VLESS(4, Constants.VLESS),
    TROJAN(5, Constants.TROJAN),
    WIREGUARD(6, Constants.WIREGUARD),
    HTTP(7, Constants.HTTP);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value }
    }
}
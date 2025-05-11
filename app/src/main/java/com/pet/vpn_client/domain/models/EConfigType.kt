package com.pet.vpn_client.domain.models

import com.pet.vpn_client.app.Constants

enum class EConfigType(val value: Int, val protocolScheme: String) {
    VMESS(1, Constants.VMESS),
    CUSTOM(2, Constants.CUSTOM),
    SHADOWSOCKS(3, Constants.SHADOWSOCKS),
    SOCKS(4, Constants.SOCKS),
    VLESS(5, Constants.VLESS),
    TROJAN(6, Constants.TROJAN),
    WIREGUARD(7, Constants.WIREGUARD),

    //    TUIC(8, AppConfig.TUIC),
    HYSTERIA2(9, Constants.HYSTERIA2),
    HTTP(10, Constants.HTTP);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value }
    }
}
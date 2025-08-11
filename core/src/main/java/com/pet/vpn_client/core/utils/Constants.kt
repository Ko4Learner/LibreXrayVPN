package com.pet.vpn_client.core.utils

object Constants {
    const val PACKAGE = "com.pet.vpn_client"
    const val TAG = "VpnApplication"

    const val AUTO_LOCALE_TAG = "auto"
    const val RU_LOCALE_TAG = "ru"
    const val EN_LOCALE_TAG = "en"

    const val WIREGUARD_LOCAL_ADDRESS_V4 = "172.16.0.2/32"
    const val WIREGUARD_LOCAL_MTU = "1420"

    const val VMESS = "vmess://"
    const val SHADOWSOCKS = "ss://"
    const val SOCKS = "socks://"
    const val HTTP = "http://"
    const val VLESS = "vless://"
    const val TROJAN = "trojan://"
    const val WIREGUARD = "wireguard://"

    const val DEFAULT_PORT = 443
    const val DEFAULT_LEVEL = 8
    const val DEFAULT_NETWORK = "tcp"
    const val TLS = "tls"
    const val REALITY = "reality"

    const val EXTRA_COMMAND = "COMMAND"
    const val COMMAND_START_SERVICE = "START_SERVICE"
    const val COMMAND_STOP_SERVICE = "STOP_SERVICE"
    const val COMMAND_RESTART_SERVICE = "RESTART_SERVICE"
}
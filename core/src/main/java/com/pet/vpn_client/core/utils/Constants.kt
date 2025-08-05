package com.pet.vpn_client.core.utils

object Constants {
    const val AUTO_LOCALE_TAG = "auto"
    const val RU_LOCALE_TAG = "ru"
    const val EN_LOCALE_TAG = "en"
    const val PREF_LANGUAGE = "pref_language"

    const val ANG_PACKAGE = "com.pet.vpn_client"
    const val TAG = "VpnApplication"

    const val DIR_ASSETS = "assets"

    const val DELAY_TEST_URL = "https://www.gstatic.com/generate_204"
    const val DELAY_TEST_URL2 = "https://www.google.com/generate_204"

    const val DNS_PROXY = "1.1.1.1"
    const val WIREGUARD_LOCAL_ADDRESS_V4 = "172.16.0.2/32"
    const val WIREGUARD_LOCAL_MTU = "1420"
    const val LOOPBACK = "127.0.0.1"

    const val VMESS = "vmess://"
    const val SHADOWSOCKS = "ss://"
    const val SOCKS = "socks://"
    const val HTTP = "http://"
    const val VLESS = "vless://"
    const val TROJAN = "trojan://"
    const val WIREGUARD = "wireguard://"

    const val GOOGLEAPIS_CN_DOMAIN = "domain:googleapis.cn"
    const val GOOGLEAPIS_COM_DOMAIN = "googleapis.com"

    const val DNS_DNSPOD_DOMAIN = "dot.pub"
    const val DNS_ALIDNS_DOMAIN = "dns.alidns.com"
    const val DNS_CLOUDFLARE_DOMAIN = "one.one.one.one"
    const val DNS_GOOGLE_DOMAIN = "dns.google"
    const val DNS_QUAD9_DOMAIN = "dns.quad9.net"
    const val DNS_YANDEX_DOMAIN = "common.dot.dns.yandex.net"

    const val DEFAULT_PORT = 443
    const val DEFAULT_SECURITY = "auto"
    const val DEFAULT_LEVEL = 8
    const val DEFAULT_NETWORK = "tcp"
    const val TLS = "tls"
    const val REALITY = "reality"
    const val HEADER_TYPE_HTTP = "http"

    val DNS_ALIDNS_ADDRESSES =
        arrayListOf("223.5.5.5", "223.6.6.6", "2400:3200::1", "2400:3200:baba::1")
    val DNS_CLOUDFLARE_ADDRESSES =
        arrayListOf("1.1.1.1", "1.0.0.1", "2606:4700:4700::1111", "2606:4700:4700::1001")
    val DNS_DNSPOD_ADDRESSES = arrayListOf("1.12.12.12", "120.53.53.53")
    val DNS_GOOGLE_ADDRESSES =
        arrayListOf("8.8.8.8", "8.8.4.4", "2001:4860:4860::8888", "2001:4860:4860::8844")
    val DNS_QUAD9_ADDRESSES = arrayListOf("9.9.9.9", "149.112.112.112", "2620:fe::fe", "2620:fe::9")
    val DNS_YANDEX_ADDRESSES =
        arrayListOf("77.88.8.8", "77.88.8.1", "2a02:6b8::feed:0ff", "2a02:6b8:0:1::feed:0ff")
}
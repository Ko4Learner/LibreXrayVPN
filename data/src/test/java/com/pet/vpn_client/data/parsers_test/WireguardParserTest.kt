package com.pet.vpn_client.data.parsers_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.data.protocol_parsers.WireguardParser
import com.pet.vpn_client.domain.models.ConfigType
import org.junit.Test

class WireguardParserTest {
    private val parser = WireguardParser()

    @Test
    fun `valid - parses host, port, keys, address, mtu, reserved, remarks`() {
        val link =
            "wireguard://privKey@wg.example.com:51820" +
                    "?address=10.0.0.2/32,fd00::2/128&publickey=PUBKEY&presharedkey=PSK&mtu=1300&reserved=1,2,3" +
                    "#My%20WG"
        val cfg = parser.parse(link)
        requireNotNull(cfg)

        assertThat(cfg.configType).isEqualTo(ConfigType.WIREGUARD)
        assertThat(cfg.server).isEqualTo("wg.example.com")
        assertThat(cfg.serverPort).isEqualTo("51820")
        assertThat(cfg.secretKey).isEqualTo("privKey")
        assertThat(cfg.publicKey).isEqualTo("PUBKEY")
        assertThat(cfg.preSharedKey).isEqualTo("PSK")
        assertThat(cfg.localAddress).isEqualTo("10.0.0.2/32,fd00::2/128")
        assertThat(cfg.mtu).isEqualTo(1300)
        assertThat(cfg.reserved).isEqualTo("1,2,3")
        assertThat(cfg.remarks).isEqualTo("My WG")
    }

    @Test
    fun `invalid - missing query returns null`() {
        val link = "wireguard://priv@wg.example.com:51820#no-query"
        assertThat(parser.parse(link)).isNull()
    }
}
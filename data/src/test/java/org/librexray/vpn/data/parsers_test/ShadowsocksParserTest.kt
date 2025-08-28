package org.librexray.vpn.data.parsers_test

import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.data.protocol_parsers.ShadowsocksParser
import org.librexray.vpn.domain.models.ConfigType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShadowsocksParserTest {
    private val parser = ShadowsocksParser()

    @Test
    fun `SIP002 plain userInfo - parses host, port, method, password, remarks`() {
        val link = "ss://aes-256-gcm:pass@example.com:8388#My%20Server"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.configType).isEqualTo(ConfigType.SHADOWSOCKS)
        assertThat(cfg.server).isEqualTo("example.com")
        assertThat(cfg.serverPort).isEqualTo("8388")
        assertThat(cfg.method).isEqualTo("aes-256-gcm")
        assertThat(cfg.password).isEqualTo("pass")
        assertThat(cfg.remarks).isEqualTo("My Server")
    }

    @Test
    fun `SIP002 base64 userInfo - parses method and password`() {
        val userInfoB64 = "Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTpzZWNyZXQ="
        val link = "ss://$userInfoB64@1.2.3.4:1080#r"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.server).isEqualTo("1.2.3.4")
        assertThat(cfg.serverPort).isEqualTo("1080")
        assertThat(cfg.method).isEqualTo("chacha20-ietf-poly1305")
        assertThat(cfg.password).isEqualTo("secret")
        assertThat(cfg.remarks).isEqualTo("r")
    }

    @Test
    fun `SIP002 invalid - missing port returns null`() {
        val link = "ss://aes-256-gcm:pass@example.com#no-port"
        val cfg = parser.parse(link)
        assertThat(cfg).isNull()
    }


    @Test
    fun `Legacy valid - parses all fields`() {
        val pre = "YWVzLTI1Ni1nY206cHdkMTIz"
        val link = "ss://$pre@example.org:8388#Name%20Here"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.server).isEqualTo("example.org")
        assertThat(cfg.serverPort).isEqualTo("8388")
        assertThat(cfg.method).isEqualTo("aes-256-gcm")
        assertThat(cfg.password).isEqualTo("pwd123")
        assertThat(cfg.remarks).isEqualTo("Name Here")
    }
}
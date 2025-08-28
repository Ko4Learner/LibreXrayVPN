package org.librexray.vpn.data.parsers_test

import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.data.protocol_parsers.SocksParser
import org.librexray.vpn.domain.models.ConfigType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SocksParserTest {
    private val parser = SocksParser()

    @Test
    fun `parses host and port without credentials`() {
        val link = "socks://example.com:1080#Node%201"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.configType).isEqualTo(ConfigType.SOCKS)
        assertThat(cfg.server).isEqualTo("example.com")
        assertThat(cfg.serverPort).isEqualTo("1080")
        assertThat(cfg.username).isNull()
        assertThat(cfg.password).isNull()
        assertThat(cfg.remarks).isEqualTo("Node 1")
    }

    @Test
    fun `parses base64 userInfo when provided`() {
        val b64 = "YWxpY2U6c2VjcmV0"
        val link = "socks://$b64@1.2.3.4:1080#b64"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.server).isEqualTo("1.2.3.4")
        assertThat(cfg.serverPort).isEqualTo("1080")
        assertThat(cfg.username).isEqualTo("alice")
        assertThat(cfg.password).isEqualTo("secret")
        assertThat(cfg.remarks).isEqualTo("b64")
    }

    @Test
    fun `invalid - missing port returns null`() {
        val link = "socks://host.example#no-port"
        assertThat(parser.parse(link)).isNull()
    }
}
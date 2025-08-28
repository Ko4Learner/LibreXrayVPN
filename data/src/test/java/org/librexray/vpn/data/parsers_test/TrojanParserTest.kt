package org.librexray.vpn.data.parsers_test

import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.data.protocol_parsers.TrojanParser
import org.librexray.vpn.domain.models.ConfigType
import org.junit.Test

class TrojanParserTest {
    private val parser = TrojanParser()

    @Test
    fun `minimal trojan without query - sets defaults TCP+TLS`() {
        val link = "trojan://secret@ex.com:443#My%20Trojan"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.configType).isEqualTo(ConfigType.TROJAN)
        assertThat(cfg.server).isEqualTo("ex.com")
        assertThat(cfg.serverPort).isEqualTo("443")
        assertThat(cfg.password).isEqualTo("secret")
        assertThat(cfg.remarks).isEqualTo("My Trojan")
        assertThat(cfg.network).isEqualTo("tcp")
        assertThat(cfg.security).isEqualTo("tls")
        assertThat(cfg.insecure).isFalse()
    }
}
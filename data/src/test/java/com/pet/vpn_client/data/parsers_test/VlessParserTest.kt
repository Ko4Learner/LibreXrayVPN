package com.pet.vpn_client.data.parsers_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.data.protocol_parsers.VlessParser
import com.pet.vpn_client.domain.models.ConfigType
import org.junit.Test


class VlessParserTest {
    private val parser = VlessParser()
    private val uuid = "123e4567-e89b-12d3-a456-426614174000"

    @Test
    fun `valid vless with query parses host, port, uuid and encryption`() {
        val link = "vless://$uuid@ex.com:443?encryption=none#Node"
        val cfg = parser.parse(link)

        requireNotNull(cfg)
        assertThat(cfg.configType).isEqualTo(ConfigType.VLESS)
        assertThat(cfg.server).isEqualTo("ex.com")
        assertThat(cfg.serverPort).isEqualTo("443")
        assertThat(cfg.password).isEqualTo(uuid)
        assertThat(cfg.method).isEqualTo("none")
        assertThat(cfg.remarks).isEqualTo("Node")
    }

    @Test
    fun `invalid - missing query returns null`() {
        val link = "vless://$uuid@ex.com:443#no-query"
        assertThat(parser.parse(link)).isNull()
    }
}
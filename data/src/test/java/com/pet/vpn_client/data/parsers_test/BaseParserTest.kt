package com.pet.vpn_client.data.parsers_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.data.protocol_parsers.BaseParser
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import org.junit.Test
import java.net.URI

class BaseParserTest {
    private val base = BaseParser()

    @Test
    fun `getQueryParam parses and url-decodes`() {
        val uri = URI("x://h?host=ex.com&fp=chrome")
        val q = base.getQueryParam(uri)

        assertThat(q["host"]).isEqualTo("ex.com")
        assertThat(q["fp"]).isEqualTo("chrome")
    }

    @Test
    fun `applies fields, defaults, and whitelist security`() {
        val cfg = ConfigProfileItem.create(ConfigType.VLESS)
        val q = base.getQueryParam(URI("x://h?host=ex.com&path=%2Ffoo&security=unknown&fp=chrome"))

        base.getItemFromQuery(cfg, q, allowInsecure = true)

        assertThat(cfg.host).isEqualTo("ex.com")
        assertThat(cfg.path).isEqualTo("/foo")
        assertThat(cfg.fingerPrint).isEqualTo("chrome")
        assertThat(cfg.network).isEqualTo("tcp")
        assertThat(cfg.security).isNull()
        assertThat(cfg.insecure).isTrue()
    }
}
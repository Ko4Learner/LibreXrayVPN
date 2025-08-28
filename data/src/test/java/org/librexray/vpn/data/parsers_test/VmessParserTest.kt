package org.librexray.vpn.data.parsers_test

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.librexray.vpn.data.protocol_parsers.VmessParser
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
class VmessParserTest {
    private val gson = Gson()
    private val parser = VmessParser(gson)
    private val uuid = "123e4567-e89b-12d3-a456-426614174000"

    @Test
    fun `base64 JSON - parses required fields`() {
        val json = """
            {"v":"2","ps":"My Node","add":"ex.com","port":"443","id":"$uuid",
             "net":"ws","type":"none","host":"cdn.example.com","path":"/api",
             "tls":"tls","sni":"sni.ex","alpn":"h2","fp":"chrome","scy":""}
        """.trimIndent().replace("\n", "")
        val b64 = Base64.getEncoder().encodeToString(json.toByteArray())
        val link = "vmess://$b64"

        val cfg = parser.parse(link)
        requireNotNull(cfg)

        assertThat(cfg.server).isEqualTo("ex.com")
        assertThat(cfg.serverPort).isEqualTo("443")
        assertThat(cfg.password).isEqualTo(uuid)
        assertThat(cfg.network).isEqualTo("ws")
        assertThat(cfg.path).isEqualTo("/api")
        assertThat(cfg.security).isEqualTo("tls")
        assertThat(cfg.remarks).isEqualTo("My Node")
    }

    @Test
    fun `uri-style invalid - empty query returns null`() {
        val link = "vmess://$uuid@ex.com:443?#frag&ment"
        assertThat(parser.parse(link)).isNull()
    }
}
package org.librexray.vpn.framework.outbound_converter_test

import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.framework.bridge_to_core.XrayConfigProvider
import org.librexray.vpn.framework.models.XrayConfig.OutboundBean
import org.librexray.vpn.framework.outbound_converter.TrojanConverter
import dagger.Lazy
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class TrojanConverterTest {
    private lateinit var provider: XrayConfigProvider
    private lateinit var lazyProvider: Lazy<XrayConfigProvider>
    private lateinit var converter: TrojanConverter

    private lateinit var outbound: OutboundBean

    @Before
    fun setup() {
        outbound = OutboundBean(
            protocol = ConfigType.TROJAN.name, settings = OutboundBean.OutSettingsBean(
                servers = mutableListOf(OutboundBean.OutSettingsBean.ServersBean())
            ), streamSettings = OutboundBean.StreamSettingsBean()
        )

        provider = mockk()
        every { provider.createInitOutbound(ConfigType.TROJAN) } returns outbound
        every {
            provider.populateTransportSettings(
                outbound.streamSettings!!,
                any()
            )
        } returns "sni.test"
        every {
            provider.populateTlsSettings(
                outbound.streamSettings!!,
                any(),
                "sni.test"
            )
        } just Runs

        lazyProvider = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = provider
        }
        converter = TrojanConverter(lazyProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `toOutbound maps address, port, password, flow and applies transport then tls`() {
        val profile = ConfigProfileItem(ConfigType.TROJAN).copy(
            server = "trojan.example.com",
            serverPort = "443",
            password = "p@ss",
            flow = "xtls-rprx-vision"
        )
        val result = converter.toOutbound(profile)
        assertThat(result).isSameInstanceAs(outbound)

        val server = result!!.settings!!.servers!!.first()
        assertThat(server.address).isEqualTo("trojan.example.com")
        assertThat(server.port).isEqualTo(443)
        assertThat(server.password).isEqualTo("p@ss")
        assertThat(server.flow).isEqualTo("xtls-rprx-vision")

        verifySequence {
            provider.createInitOutbound(ConfigType.TROJAN)
            provider.populateTransportSettings(outbound.streamSettings!!, profile)
            provider.populateTlsSettings(outbound.streamSettings!!, profile, "sni.test")
        }
    }

    @Test
    fun `toOutbound returns null when provider returns null`() {
        val nullProvider = mockk<XrayConfigProvider>(relaxUnitFun = true) {
            every { createInitOutbound(ConfigType.TROJAN) } returns null
        }
        val nullLazy = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = nullProvider
        }
        val nullConverter = TrojanConverter(nullLazy)

        val profile = ConfigProfileItem.create(ConfigType.TROJAN).copy(
            server = "host",
            serverPort = "1"
        )

        val result = nullConverter.toOutbound(profile)

        assertThat(result).isNull()
        verify(exactly = 1) { nullProvider.createInitOutbound(ConfigType.TROJAN) }
        verify(exactly = 0) { nullProvider.populateTransportSettings(any(), any()) }
        verify(exactly = 0) { nullProvider.populateTlsSettings(any(), any(), any()) }
    }
}
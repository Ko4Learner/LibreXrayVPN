package com.pet.vpn_client.framework.outbound_converter_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.framework.bridge_to_core.XrayConfigProvider
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.outbound_converter.ShadowsocksConverter
import dagger.Lazy
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class ShadowsocksConverterTest {
    private lateinit var provider: XrayConfigProvider
    private lateinit var lazyProvider: Lazy<XrayConfigProvider>
    private lateinit var converter: ShadowsocksConverter

    private lateinit var outbound: OutboundBean

    @Before
    fun setup() {
        outbound = OutboundBean(
            protocol = ConfigType.SHADOWSOCKS.name,
            settings = OutboundBean.OutSettingsBean(
                servers = mutableListOf(OutboundBean.OutSettingsBean.ServersBean())
            ),
            streamSettings = OutboundBean.StreamSettingsBean()
        )

        provider = mockk(relaxUnitFun = true)

        every { provider.createInitOutbound(ConfigType.SHADOWSOCKS) } returns outbound
        val sni = "sni.test"
        every {
            provider.populateTransportSettings(outbound.streamSettings!!, any())
        } returns sni
        every {
            provider.populateTlsSettings(outbound.streamSettings!!, any(), sni)
        } just Runs

        lazyProvider = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = provider
        }

        converter = ShadowsocksConverter(lazyProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `toOutbound maps server, port, method, password and applies transport+tls`() {
        val profile = ConfigProfileItem.create(ConfigType.SHADOWSOCKS).copy(
            server = "1.2.3.4",
            serverPort = "8388",
            method = "aes-256-gcm",
            password = "p@ss"
        )

        val result = converter.toOutbound(profile)

        assertThat(result).isSameInstanceAs(outbound)

        val server = result!!.settings!!.servers!!.first()
        assertThat(server.address).isEqualTo("1.2.3.4")
        assertThat(server.port).isEqualTo(8388)
        assertThat(server.method).isEqualTo("aes-256-gcm")
        assertThat(server.password).isEqualTo("p@ss")

        verifySequence {
            provider.createInitOutbound(ConfigType.SHADOWSOCKS)
            provider.populateTransportSettings(outbound.streamSettings!!, profile)
            provider.populateTlsSettings(outbound.streamSettings!!, profile, "sni.test")
        }
    }

    @Test
    fun `toOutbound returns null and does nothing when provider returned null`() {
        val nullProvider = mockk<XrayConfigProvider>(relaxUnitFun = true) {
            every { createInitOutbound(ConfigType.SHADOWSOCKS) } returns null
        }
        val nullLazy = object : Lazy<XrayConfigProvider> {
            override fun get() = nullProvider
        }
        val nullConverter = ShadowsocksConverter(nullLazy)

        val profile = ConfigProfileItem.create(ConfigType.SHADOWSOCKS).copy(
            server = "host",
            serverPort = "1"
        )

        val result = nullConverter.toOutbound(profile)

        assertThat(result).isNull()
        verify(exactly = 1) { nullProvider.createInitOutbound(ConfigType.SHADOWSOCKS) }
        verify(exactly = 0) { nullProvider.populateTransportSettings(any(), any()) }
        verify(exactly = 0) { nullProvider.populateTlsSettings(any(), any(), any()) }
    }
}
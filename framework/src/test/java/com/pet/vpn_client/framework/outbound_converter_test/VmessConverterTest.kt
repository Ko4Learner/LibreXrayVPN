package com.pet.vpn_client.framework.outbound_converter_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.framework.bridge_to_core.XrayConfigProvider
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.outbound_converter.VmessConverter
import dagger.Lazy
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class VmessConverterTest {
    private lateinit var provider: XrayConfigProvider
    private lateinit var lazyProvider: Lazy<XrayConfigProvider>
    private lateinit var converter: VmessConverter

    private lateinit var outbound: OutboundBean

    @Before
    fun setup() {
        outbound = OutboundBean(
            protocol = ConfigType.VMESS.name,
            settings = OutboundBean.OutSettingsBean(
                vnext = listOf(
                    OutboundBean.OutSettingsBean.VnextBean(
                        address = "",
                        port = 0,
                        users = listOf(OutboundBean.OutSettingsBean.VnextBean.UsersBean())
                    )
                )
            ),
            streamSettings = OutboundBean.StreamSettingsBean()
        )

        provider = mockk(relaxUnitFun = true)

        every { provider.createInitOutbound(ConfigType.VMESS) } returns outbound
        every { provider.populateTransportSettings(outbound.streamSettings!!, any()) } returns "sni.test"
        every { provider.populateTlsSettings(outbound.streamSettings!!, any(), "sni.test") } just Runs

        lazyProvider = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = provider
        }

        converter = VmessConverter(lazyProvider)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `toOutbound maps address, port and user id, security then applies transport and tls`() {
        val profile = ConfigProfileItem.create(ConfigType.VMESS).copy(
            server = "vmess.example.com",
            serverPort = "443",
            password = "uuid-123",       // id
            method = "auto"              // security
        )

        val result = converter.toOutbound(profile)

        assertThat(result).isSameInstanceAs(outbound)

        val vnext = result!!.settings!!.vnext!!.first()
        assertThat(vnext.address).isEqualTo("vmess.example.com")
        assertThat(vnext.port).isEqualTo(443)
        assertThat(vnext.users[0].id).isEqualTo("uuid-123")
        assertThat(vnext.users[0].security).isEqualTo("auto")

        verifySequence {
            provider.createInitOutbound(ConfigType.VMESS)
            provider.populateTransportSettings(outbound.streamSettings!!, profile)
            provider.populateTlsSettings(outbound.streamSettings!!, profile, "sni.test")
        }
    }

    @Test
    fun `toOutbound returns null when provider returns null`() {
        val nullProvider = mockk<XrayConfigProvider>(relaxUnitFun = true) {
            every { createInitOutbound(ConfigType.VMESS) } returns null
        }
        val nullLazy = object : Lazy<XrayConfigProvider> { override fun get() = nullProvider }
        val nullConverter = VmessConverter(nullLazy)

        val profile = ConfigProfileItem.create(ConfigType.VMESS).copy(
            server = "any",
            serverPort = "1"
        )

        val result = nullConverter.toOutbound(profile)

        assertThat(result).isNull()
        verify(exactly = 1) { nullProvider.createInitOutbound(ConfigType.VMESS) }
        verify(exactly = 0) { nullProvider.populateTransportSettings(any(), any()) }
        verify(exactly = 0) { nullProvider.populateTlsSettings(any(), any(), any()) }
    }
}
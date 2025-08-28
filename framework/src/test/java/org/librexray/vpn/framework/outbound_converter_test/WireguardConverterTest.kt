package org.librexray.vpn.framework.outbound_converter_test

import com.google.common.truth.Truth.assertThat
import org.librexray.vpn.core.utils.Utils
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.framework.bridge_to_core.XrayConfigProvider
import org.librexray.vpn.framework.models.XrayConfig.OutboundBean
import org.librexray.vpn.framework.outbound_converter.WireguardConverter
import dagger.Lazy
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class WireguardConverterTest {
    private lateinit var provider: XrayConfigProvider
    private lateinit var lazyProvider: Lazy<XrayConfigProvider>
    private lateinit var converter: WireguardConverter

    private lateinit var outbound: OutboundBean

    @Before
    fun setup() {
        outbound = OutboundBean(
            protocol = ConfigType.WIREGUARD.name,
            settings = OutboundBean.OutSettingsBean(
                peers = mutableListOf(OutboundBean.OutSettingsBean.WireGuardBean())
            )
        )

        provider = mockk()
        every { provider.createInitOutbound(ConfigType.WIREGUARD) } returns outbound

        lazyProvider = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = provider
        }

        converter = WireguardConverter(lazyProvider)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `toOutbound maps keys, addresses, peers, mtu and reserved`() {
        val profile = ConfigProfileItem.create(ConfigType.WIREGUARD).copy(
            server = "10.0.0.5",
            serverPort = "51820",
            secretKey = "secretKey123",
            publicKey = "peerPublic",
            preSharedKey = "psk",
            localAddress = "192.168.1.2/24,fd00::1/64",
            mtu = 1420,
            reserved = "1, 2,3"
        )

        val result = converter.toOutbound(profile)

        assertThat(result).isSameInstanceAs(outbound)
        val wg = result!!.settings!!

        assertThat(wg.secretKey).isEqualTo("secretKey123")
        assertThat(wg.address).isEqualTo(listOf("192.168.1.2/24", "fd00::1/64"))

        val peer = wg.peers!!.first()
        assertThat(peer.publicKey).isEqualTo("peerPublic")
        assertThat(peer.preSharedKey).isEqualTo("psk")
        assertThat(peer.endpoint).isEqualTo(Utils.getIpv6Address("10.0.0.5") + ":51820")

        assertThat(wg.mtu).isEqualTo(1420)
        assertThat(wg.reserved).containsExactly(1, 2, 3)

        verify(exactly = 1) { provider.createInitOutbound(ConfigType.WIREGUARD) }
    }

    @Test
    fun `toOutbound applies defaults when reserved empty and preSharedKey blank`() {
        val profile = ConfigProfileItem.create(ConfigType.WIREGUARD).copy(
            server = "1.1.1.1",
            serverPort = "1234",
            secretKey = "s",
            publicKey = "pub",
            preSharedKey = "",
            reserved = ""
        )

        val result = converter.toOutbound(profile)

        val wg = result!!.settings!!
        val peer = wg.peers!!.first()
        assertThat(peer.preSharedKey).isNull()
        assertThat(wg.reserved).isNull()
    }

    @Test
    fun `toOutbound returns null when provider returns null`() {
        val nullProvider = mockk<XrayConfigProvider> {
            every { createInitOutbound(ConfigType.WIREGUARD) } returns null
        }
        val nullLazy = object : Lazy<XrayConfigProvider> { override fun get() = nullProvider }
        val nullConverter = WireguardConverter(nullLazy)

        val result = nullConverter.toOutbound(
            ConfigProfileItem.create(ConfigType.WIREGUARD)
        )

        assertThat(result).isNull()
        verify(exactly = 1) { nullProvider.createInitOutbound(ConfigType.WIREGUARD) }
    }
}
package com.pet.vpn_client.framework.outbound_converter_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.framework.bridge_to_core.XrayConfigProvider
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.outbound_converter.SocksConverter
import dagger.Lazy
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class SocksConverterTest {
    private lateinit var provider: XrayConfigProvider
    private lateinit var lazyProvider: Lazy<XrayConfigProvider>
    private lateinit var converter: SocksConverter

    private lateinit var outbound: OutboundBean

    @Before
    fun setup() {
        outbound = OutboundBean(
            protocol = ConfigType.SOCKS.name,
            settings = OutboundBean.OutSettingsBean(
                servers = mutableListOf(OutboundBean.OutSettingsBean.ServersBean())
            )
        )

        provider = mockk()
        every { provider.createInitOutbound(ConfigType.SOCKS) } returns outbound

        lazyProvider = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = provider
        }

        converter = SocksConverter(lazyProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `toOutbound maps address, port and users when username present`() {
        val profile = ConfigProfileItem.create(ConfigType.SOCKS).copy(
            server = "127.0.0.1",
            serverPort = "1080",
            username = "user1",
            password = "pass1"
        )

        val result = converter.toOutbound(profile)

        verify(exactly = 1) { provider.createInitOutbound(ConfigType.SOCKS) }
        assertThat(result).isSameInstanceAs(outbound)

        val server = result!!.settings!!.servers!!.first()
        assertThat(server.address).isEqualTo("127.0.0.1")
        assertThat(server.port).isEqualTo(1080)

        val users = server.users
        assertThat(users).isNotNull()
        assertThat(users!!.size).isEqualTo(1)
        assertThat(users[0].user).isEqualTo("user1")
        assertThat(users[0].pass).isEqualTo("pass1")
    }

    @Test
    fun `toOutbound leaves users null when username empty`() {
        val profile = ConfigProfileItem.create(ConfigType.SOCKS).copy(
            server = "socks.example.com",
            serverPort = "9050",
            username = "",
            password = "ignored"
        )

        val result = converter.toOutbound(profile)

        verify(exactly = 1) { provider.createInitOutbound(ConfigType.SOCKS) }
        val server = result!!.settings!!.servers!!.first()
        assertThat(server.address).isEqualTo("socks.example.com")
        assertThat(server.port).isEqualTo(9050)
        assertThat(server.users).isNull()
    }
}
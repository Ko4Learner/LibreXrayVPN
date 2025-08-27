package com.pet.vpn_client.framework.outbound_converter_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.framework.bridge_to_core.XrayConfigProvider
import com.pet.vpn_client.framework.models.XrayConfig.OutboundBean
import com.pet.vpn_client.framework.outbound_converter.HttpConverter
import dagger.Lazy
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class HttpConverterTest {
    private lateinit var provider: XrayConfigProvider
    private lateinit var lazyProvider: Lazy<XrayConfigProvider>
    private lateinit var converter: HttpConverter

    private lateinit var outbound: OutboundBean

    @Before
    fun setup() {
        outbound = OutboundBean(
            protocol = ConfigType.HTTP.name,
            settings = OutboundBean.OutSettingsBean(
                servers = mutableListOf(OutboundBean.OutSettingsBean.ServersBean())
            )
        )

        provider = mockk()
        every { provider.createInitOutbound(ConfigType.HTTP) } returns outbound

        lazyProvider = object : Lazy<XrayConfigProvider> {
            override fun get(): XrayConfigProvider = provider
        }

        converter = HttpConverter(lazyProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `toOutbound maps address, port and basic auth`() {
        val profile = ConfigProfileItem.create(ConfigType.HTTP).copy(
            server = "10.0.0.5",
            serverPort = "8080",
            username = "alice",
            password = "secret"
        )

        val result = converter.toOutbound(profile)

        verify(exactly = 1) { provider.createInitOutbound(ConfigType.HTTP) }
        assertThat(result).isSameInstanceAs(outbound)

        val server = result!!.settings!!.servers!!.first()
        assertThat(server.address).isEqualTo("10.0.0.5")
        assertThat(server.port).isEqualTo(8080)

        val users = server.users
        assertThat(users).isNotNull()
        assertThat(users!!.size).isEqualTo(1)
        assertThat(users[0].user).isEqualTo("alice")
        assertThat(users[0].pass).isEqualTo("secret")
    }

    @Test
    fun `toOutbound leaves users null when username empty`() {
        val profile = ConfigProfileItem.create(ConfigType.HTTP).copy(
            server = "host.example.com",
            serverPort = "80",
            username = "",
            password = "ignored"
        )

        val result = converter.toOutbound(profile)

        verify(exactly = 1) { provider.createInitOutbound(ConfigType.HTTP) }

        val server = result!!.settings!!.servers!!.first()
        assertThat(server.address).isEqualTo("host.example.com")
        assertThat(server.port).isEqualTo(80)
        assertThat(server.users).isNull()
    }
}
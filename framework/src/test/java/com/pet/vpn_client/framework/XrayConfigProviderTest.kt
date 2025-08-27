package com.pet.vpn_client.framework

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigResult
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.framework.bridge_to_core.XrayConfigProvider
import com.pet.vpn_client.framework.models.XrayConfig
import com.pet.vpn_client.framework.outbound_converter.HttpConverter
import com.pet.vpn_client.framework.outbound_converter.ShadowsocksConverter
import com.pet.vpn_client.framework.outbound_converter.SocksConverter
import com.pet.vpn_client.framework.outbound_converter.TrojanConverter
import com.pet.vpn_client.framework.outbound_converter.VlessConverter
import com.pet.vpn_client.framework.outbound_converter.VmessConverter
import com.pet.vpn_client.framework.outbound_converter.WireguardConverter
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class XrayConfigProviderTest {

    private val gson = Gson()
    private lateinit var storage: KeyValueStorage
    private lateinit var http: HttpConverter
    private lateinit var ss: ShadowsocksConverter
    private lateinit var socks: SocksConverter
    private lateinit var trojan: TrojanConverter
    private lateinit var vless: VlessConverter
    private lateinit var vmess: VmessConverter
    private lateinit var wg: WireguardConverter
    private lateinit var context: Context

    private lateinit var provider: XrayConfigProvider

    private val baseJson = """
      {
        "log": { "loglevel": "warning" },
        "remarks": "",
        "routing": { "domainStrategy": "" },
        "inbounds": [
          { "port": 0, "listen": "", "sniffing": { "enabled": false, "routeOnly": false } },
          {}
        ],
        "outbounds": [],
        "dns": null,
        "policy": { "system": { "statsOutboundUplink": true, "statsOutboundDownlink": true } }
      }
    """.trimIndent()

    @Before
    fun setUp() {
        storage = mockk()
        http = mockk()
        ss = mockk()
        socks = mockk()
        trojan = mockk()
        vless = mockk()
        vmess = mockk()
        wg = mockk()
        context = mockk(relaxed = true)

        provider = XrayConfigProvider(
            storage = storage,
            gson = gson,
            httpConverter = http,
            shadowsocksConverter = ss,
            socksConverter = socks,
            trojanConverter = trojan,
            vlessConverter = vless,
            vmessConverter = vmess,
            wireguardConverter = wg,
            context = context
        )

        val field = XrayConfigProvider::class.java.getDeclaredField("initConfigCache")
        field.isAccessible = true
        field.set(provider, baseJson)
    }

    @After fun tearDown() = unmockkAll()

    @Test
    fun `getCoreConfig builds WireGuard config, trims address and disables mux`() {
        val guid = "wg-1"
        val profile = ConfigProfileItem(
            configType = ConfigType.WIREGUARD,
            remarks = "WG Node",
            server = "1.2.3.4"
        )
        every { storage.decodeServerConfig(guid) } returns profile

        val settings = XrayConfig.OutboundBean.OutSettingsBean(
            secretKey = "SECRET",
            address = listOf("10.0.0.2/32", "fd00::1/128"),
            peers = listOf(
                XrayConfig.OutboundBean.OutSettingsBean.WireGuardBean(
                    publicKey = "PUBKEY"
                )
            ),
            mtu = 1280,
            reserved = listOf(0, 0, 0)
        )

        val outbound = XrayConfig.OutboundBean(
            protocol = "wireguard",
            settings = settings,
            mux = XrayConfig.OutboundBean.MuxBean(enabled = true, concurrency = 8)
        )
        every { wg.toOutbound(profile) } returns outbound

        val res: ConfigResult = provider.getCoreConfig(guid)

        assertThat(res.status).isTrue()
        assertThat(res.guid).isEqualTo(guid)
        val json = res.content
        assertThat(json).contains("\"protocol\": \"wireguard\"")

        assertThat(json).contains("\"address\": [")
        assertThat(json).contains("\"10.0.0.2/32\"")
        assertThat(json).doesNotContain("fd00::1/128")

        assertThat(json).contains("\"enabled\": false")
        assertThat(json).contains("\"concurrency\": -1")

        assertThat(json).contains("\"domainStrategy\": \"IPIfNonMatch\"")
    }

    @Test
    fun `getCoreConfig fails on invalid server`() {
        val guid = "bad"
        val profile = ConfigProfileItem(
            configType = ConfigType.WIREGUARD,
            remarks = "Bad WG",
            server = "not a host !!!"
        )
        every { storage.decodeServerConfig(guid) } returns profile

        val res = provider.getCoreConfig(guid)

        assertThat(res.status).isFalse()
        assertThat(res.content).isAnyOf(null, "")
    }
}

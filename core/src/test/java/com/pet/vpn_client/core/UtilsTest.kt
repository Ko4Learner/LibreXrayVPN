package com.pet.vpn_client.core

import android.util.Base64
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.Utils.fromJsonReified
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilsTest {

    // Base64
    @Test
    fun decode_standardBase64() {
        val original = "xray-core"
        val encoded = Base64.encodeToString(original.toByteArray(), Base64.NO_WRAP)
        assertThat(Utils.decode(encoded)).isEqualTo(original)
    }
    @Test
    fun decode_urlSafeBase64_withoutPadding() {
        val original = "vpn+core/path"
        val urlSafe = Base64.encodeToString(
            original.toByteArray(),
            Base64.NO_WRAP or Base64.URL_SAFE
        ).trimEnd('=')
        assertThat(Utils.decode(urlSafe)).isEqualTo(original)
    }

    // IP
    @Test
    fun isIpAddress_ipv4MappedWithPort() {
        val input = "[::ffff:192.168.0.1]:8080"
        assertThat(Utils.isIpAddress(input)).isTrue()
    }
    @Test
    fun isIpAddress_pureIpv6() {
        assertThat(Utils.isIpAddress("2001:db8::1")).isTrue()
    }

    // URL
    @Test
    fun isValidUrl_https() {
        assertThat(Utils.isValidUrl("https://example.com")).isTrue()
    }

    // IPv6 wrap
    @Test
    fun getIpv6Address_wrapsWhenNeeded() {
        assertThat(Utils.getIpv6Address("2001:db8::1"))
            .isEqualTo("[2001:db8::1]")
    }

    // JSON
    data class Node(val host: String, val port: Int)

    @Test
    fun fromJsonReified_parsesTypedObject() {
        val json = """{"host":"example.com","port":443}"""
        val node: Node = Gson().fromJsonReified(json)
        assertThat(node).isEqualTo(Node("example.com", 443))
    }
}
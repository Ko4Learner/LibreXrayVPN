package com.pet.vpn_client.core

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.core.utils.idnHost
import org.junit.Test
import java.net.URI

class ExtensionsTest {

    @Test
    fun `normal domain returns host`() {
        val uri = URI("https://example.com")
        assertThat(uri.idnHost).isEqualTo("example.com")
    }

    @Test
    fun `ipv6 host is returned without brackets`() {
        val uri = URI("http://[2001:db8::1]:8080")
        assertThat(uri.idnHost).isEqualTo("2001:db8::1")
    }
}
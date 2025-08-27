package com.pet.vpn_client.framework.vpn_service_test

import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.models.ConnectionSpeed
import com.pet.vpn_client.framework.services.VPNService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class SpeedFormattingTest {
    private lateinit var service: VPNService

    @Before
    fun setUp() {
        service = Robolectric.buildService(VPNService::class.java).get()
    }

    @Test
    fun `fmtBps formats bytes under 1KB`() {
        val out = service.fmtBps(999.0)
        assertThat(out).isEqualTo("999 B/s")
    }

    @Test
    fun `fmtBps formats kilobytes at 1KB`() {
        val out = service.fmtBps(1024.0)
        assertThat(out).isEqualTo("1 KB/s")
    }

    @Test
    fun `fmtBps formats megabytes with two decimals`() {
        val out = service.fmtBps(2_621_440.0)
        assertThat(out).isEqualTo("2.50 MB/s")
    }

    @Test
    fun `formatSpeedLine contains Proxy and Direct labels`() {
        val s = ConnectionSpeed(
            proxyUplinkBps = 1024.0,
            proxyDownlinkBps = 0.0,
            directUplinkBps = 0.0,
            directDownlinkBps = 0.0
        )
        val line = service.formatSpeedLine(s)

        assertThat(line).contains(Constants.LABEL_PROXY)
        assertThat(line).contains(Constants.LABEL_DIRECT)
        assertThat(line).contains("1 KB/s")
    }
}
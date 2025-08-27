package com.pet.vpn_client.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pet.vpn_client.domain.interactor_impl.ConnectionInteractorImpl
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.models.ConnectionSpeed
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class ConnectionInteractorImplTest {
    private val serviceManager = mockk<ServiceManager>(relaxed = true)
    private val coreVpnBridge = mockk<CoreVpnBridge>()

    @Test
    fun `computes correct bps on second sample`() = runBlocking {
        every { coreVpnBridge.queryStats("proxy", "uplink") } returnsMany listOf(0L, 3000L)
        every { coreVpnBridge.queryStats("proxy", "downlink") } returnsMany listOf(0L, 6000L)
        every { coreVpnBridge.queryStats("direct", "uplink") } returnsMany listOf(0L, 1000L)
        every { coreVpnBridge.queryStats("direct", "downlink") } returnsMany listOf(0L, 2000L)

        val interactor = ConnectionInteractorImpl(serviceManager, coreVpnBridge)

        interactor.observeSpeed().test {
            assertThat(awaitItem()).isEqualTo(ConnectionSpeed(0.0, 0.0, 0.0, 0.0))

            ShadowSystemClock.advanceBy(Duration.ofSeconds(3))

            val second = withTimeout(5.seconds) { awaitItem() }

            assertThat(second.proxyUplinkBps).isWithin(0.001).of(1000.0)
            assertThat(second.proxyDownlinkBps).isWithin(0.001).of(2000.0)
            assertThat(second.directUplinkBps).isWithin(0.01).of(333.3333)
            assertThat(second.directDownlinkBps).isWithin(0.01).of(666.6666)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `negative deltas are clamped to zero`() = runBlocking {
        every { coreVpnBridge.queryStats("proxy", "uplink") } returnsMany listOf(500L, 500L)
        every { coreVpnBridge.queryStats("proxy", "downlink") } returnsMany listOf(500L, 500L)
        every { coreVpnBridge.queryStats("direct", "uplink") } returnsMany listOf(500L, 500L)
        every { coreVpnBridge.queryStats("direct", "downlink") } returnsMany listOf(500L, 500L)

        val interactor = ConnectionInteractorImpl(serviceManager, coreVpnBridge)

        interactor.observeSpeed().test(timeout = 10.seconds) {
            awaitItem()

            ShadowSystemClock.advanceBy(Duration.ofSeconds(3))

            val second = withTimeout(5.seconds) { awaitItem() }

            assertThat(second.proxyUplinkBps).isEqualTo(0.0)
            assertThat(second.proxyDownlinkBps).isEqualTo(0.0)
            assertThat(second.directUplinkBps).isEqualTo(0.0)
            assertThat(second.directDownlinkBps).isEqualTo(0.0)

            cancelAndConsumeRemainingEvents()
        }
    }
}
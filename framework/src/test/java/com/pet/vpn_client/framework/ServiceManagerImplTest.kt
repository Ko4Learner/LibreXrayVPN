package com.pet.vpn_client.framework

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.domain.interfaces.CoreVpnBridge
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.ConfigType
import com.pet.vpn_client.domain.state.ServiceState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class ServiceManagerImplTest {
    private lateinit var context: Context
    private lateinit var storage: KeyValueStorage
    private lateinit var bridge: CoreVpnBridge
    private lateinit var bridgeLazy: dagger.Lazy<CoreVpnBridge>
    private lateinit var stateRepo: ServiceStateRepository

    private val serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    private val coreState = MutableStateFlow(false)

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var serviceManager: ServiceManagerImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        context =
            spyk(ApplicationProvider.getApplicationContext<Context>(), recordPrivateCalls = true)
        storage = mockk(relaxed = true)

        bridge = mockk(relaxed = true)
        every { bridge.coreState } returns coreState
        bridgeLazy = mockk()
        every { bridgeLazy.get() } returns bridge

        stateRepo = mockk(relaxed = true)
        every { stateRepo.serviceState } returns serviceState

        serviceManager = ServiceManagerImpl(storage, bridgeLazy, stateRepo, context)

        ServiceManagerImpl::class.java.getDeclaredField("scope").apply {
            isAccessible = true
            set(serviceManager, testScope)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `startService - starts foreground on valid config and core not running`() = runTest {
        every { storage.getSelectedServer() } returns "guid"
        every { storage.decodeServerConfig("guid") } returns ConfigProfileItem(
            configType = ConfigType.VMESS,
            server = "https://ok",
            remarks = "srv"
        )
        mockkObject(Utils)
        every { Utils.isValidUrl(any()) } returns true
        every { Utils.isIpAddress(any()) } returns false
        coreState.value = false

        serviceManager.startService()

        verify {
            context.startForegroundService(
                match { it.getStringExtra(Constants.EXTRA_COMMAND) == Constants.COMMAND_START_SERVICE }
            )
        }
    }

    @Test
    fun `restartService - stops then waits for Idle Stopped and core false then starts`() =
        runTest {
            every { storage.getSelectedServer() } returns "guid"
            every { storage.decodeServerConfig("guid") } returns ConfigProfileItem(
                configType = ConfigType.VMESS,
                server = "https://ok",
                remarks = "srv"
            )
            mockkObject(Utils)
            every { Utils.isValidUrl(any()) } returns true
            every { Utils.isIpAddress(any()) } returns false

            serviceManager.restartService()

            verify {
                context.startForegroundService(
                    match { it.getStringExtra(Constants.EXTRA_COMMAND) == Constants.COMMAND_STOP_SERVICE }
                )
            }

            coreState.value = true
            serviceState.value = ServiceState.Connected
            advanceUntilIdle()
            verify(exactly = 0) {
                context.startForegroundService(
                    match { it.getStringExtra(Constants.EXTRA_COMMAND) == Constants.COMMAND_START_SERVICE }
                )
            }

            coreState.value = false
            serviceState.value = ServiceState.Stopped
            advanceUntilIdle()
            verify {
                context.startForegroundService(
                    match { it.getStringExtra(Constants.EXTRA_COMMAND) == Constants.COMMAND_START_SERVICE }
                )
            }
        }
}
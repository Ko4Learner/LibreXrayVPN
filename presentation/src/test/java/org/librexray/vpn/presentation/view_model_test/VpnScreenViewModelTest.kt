package org.librexray.vpn.presentation.view_model_test

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.interfaces.interactor.ConnectionInteractor
import org.librexray.vpn.domain.interfaces.interactor.SettingsInteractor
import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.domain.models.ConnectionSpeed
import org.librexray.vpn.domain.models.ImportResult
import org.librexray.vpn.domain.state.ServiceState
import org.librexray.vpn.presentation.intent.VpnScreenIntent
import org.librexray.vpn.presentation.state.VpnScreenError
import org.librexray.vpn.presentation.view_model.VpnScreenViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class VpnScreenViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var configInteractor: ConfigInteractor
    private lateinit var connectionInteractor: ConnectionInteractor
    private lateinit var settingsInteractor: SettingsInteractor
    private lateinit var stateRepository: ServiceStateRepository
    private lateinit var serviceStateFlow: MutableStateFlow<ServiceState>
    private lateinit var speedFlow: MutableSharedFlow<ConnectionSpeed>
    private lateinit var vm: VpnScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        configInteractor = mockk(relaxed = true)
        connectionInteractor = mockk(relaxed = true)
        settingsInteractor = mockk(relaxed = true)
        stateRepository = mockk()

        serviceStateFlow = MutableStateFlow(ServiceState.Idle)
        every { stateRepository.serviceState } returns serviceStateFlow

        speedFlow = MutableSharedFlow()
        every { connectionInteractor.observeSpeed() } returns speedFlow

        coEvery { configInteractor.getServerList() } returns listOf("a", "b")
        coEvery { configInteractor.getSelectedServer() } returns "a"
        coEvery { configInteractor.getServerConfig("a") } returns sampleProfile("A")
        coEvery { configInteractor.getServerConfig("b") } returns sampleProfile("B")

        vm = VpnScreenViewModel(
            configInteractor = configInteractor,
            connectionInteractor = connectionInteractor,
            settingsInteractor = settingsInteractor,
            stateRepository = stateRepository,
            io = dispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads server list and selected`() = runTest {
        val s = vm.state.value
        assertThat(s.isLoading).isFalse()
        assertThat(s.serverItemList).hasSize(2)
        assertThat(s.selectedServerId).isEqualTo("a")
    }

    @Test
    fun `service state Connected maps to isRunning=true`() = runTest {
        serviceStateFlow.value = ServiceState.Connected
        advanceUntilIdle()
        assertThat(vm.state.value.isRunning).isTrue()

        serviceStateFlow.value = ServiceState.Stopped
        advanceUntilIdle()
        assertThat(vm.state.value.isRunning).isFalse()
    }

    @Test
    fun `toggleConnection starts when not running, stops when running`() = runTest {
        coEvery { connectionInteractor.startConnection() } just Runs
        coEvery { connectionInteractor.stopConnection() } just Runs

        vm.onIntent(VpnScreenIntent.ToggleConnection)
        advanceUntilIdle()
        coVerify { connectionInteractor.startConnection() }

        serviceStateFlow.value = ServiceState.Connected
        advanceUntilIdle()
        vm.onIntent(VpnScreenIntent.ToggleConnection)
        advanceUntilIdle()
        coVerify { connectionInteractor.stopConnection() }
    }

    @Test
    fun `import from clipboard - success updates list, error sets error`() = runTest {
        coEvery { configInteractor.importClipboardConfig() } returns ImportResult.Success
        coEvery { configInteractor.getServerList() } returns listOf("x")
        coEvery { configInteractor.getServerConfig("x") } returns sampleProfile("X")

        vm.onIntent(VpnScreenIntent.ImportConfigFromClipboard)
        advanceUntilIdle()
        assertThat(vm.state.value.serverItemList).hasSize(1)
        assertThat(vm.state.value.error).isNull()

        coEvery { configInteractor.importClipboardConfig() } returns ImportResult.Error
        vm.onIntent(VpnScreenIntent.ImportConfigFromClipboard)
        advanceUntilIdle()
        assertThat(vm.state.value.error).isEqualTo(VpnScreenError.ImportConfigError)
        vm.onIntent(VpnScreenIntent.ConsumeError)
        assertThat(vm.state.value.error).isNull()

        coEvery { configInteractor.importClipboardConfig() } returns ImportResult.Empty
        vm.onIntent(VpnScreenIntent.ImportConfigFromClipboard)
        advanceUntilIdle()
        assertThat(vm.state.value.error).isEqualTo(VpnScreenError.EmptyConfigError)
    }

    @Test
    fun `deleteItem success removes and reselects first if needed`() = runTest {
        coEvery { configInteractor.deleteItem("a") } just Runs
        coEvery { configInteractor.setSelectedServer("b") } just Runs

        vm.onIntent(VpnScreenIntent.DeleteItem("a"))
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.serverItemList.map { it.guid }).containsExactly("b")
        assertThat(s.selectedServerId).isEqualTo("b")
        assertThat(s.error).isNull()
        coVerify { configInteractor.setSelectedServer("b") }
    }

    @Test
    fun `testConnection sets delay on success, error on failure`() = runTest {
        coEvery { connectionInteractor.testConnection() } returns 123L
        vm.onIntent(VpnScreenIntent.TestConnection)
        advanceUntilIdle()
        assertThat(vm.state.value.delay).isEqualTo(123L)
        assertThat(vm.state.value.error).isNull()

        coEvery { connectionInteractor.testConnection() } throws RuntimeException("x")
        vm.onIntent(VpnScreenIntent.TestConnection)
        advanceUntilIdle()
        assertThat(vm.state.value.error).isEqualTo(VpnScreenError.TestConnectionError)
    }


    @Test
    fun `markNotificationAsked updates flag and calls interactor`() = runTest {
        coEvery { settingsInteractor.markNotificationAsked() } just Runs
        vm.onIntent(VpnScreenIntent.MarkNotificationAsked)
        advanceUntilIdle()
        assertThat(vm.state.value.wasNotificationPermissionAsked).isTrue()
        coVerify { settingsInteractor.markNotificationAsked() }
    }

    private fun sampleProfile(name: String) = ConfigProfileItem(
        configType = ConfigType.VMESS,
        remarks = "Srv-$name",
        server = "10.0.0.1",
        serverPort = "443"
    )
}
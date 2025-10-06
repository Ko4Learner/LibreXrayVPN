package org.librexray.vpn.presentation.view_model_test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import androidx.camera.core.ImageProxy
import org.librexray.vpn.domain.interfaces.interactor.ConfigInteractor
import org.librexray.vpn.domain.models.FrameData
import org.librexray.vpn.domain.models.ImportResult
import org.librexray.vpn.presentation.intent.QrCodeScreenIntent
import org.librexray.vpn.presentation.mapper.toFrameData
import org.librexray.vpn.presentation.state.QrCodeScreenState
import org.librexray.vpn.presentation.view_model.QrCodeScreenViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class QrCodeScreenViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var interactor: ConfigInteractor
    private lateinit var vm: QrCodeScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        interactor = mockk(relaxed = true)
        vm = QrCodeScreenViewModel(interactor, io = dispatcher)
        mockkStatic(ImageProxy::toFrameData)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Success - sets configFound true, closes image`() {
        val image = imageReturning()
        coEvery { interactor.importQrCodeConfig(any()) } returns ImportResult.Success

        vm.onAnalyzeFrame(image)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value).isEqualTo(QrCodeScreenState(configFound = true, error = false))
        verify { image.close() }
        coVerify(exactly = 1) { interactor.importQrCodeConfig(any()) }
    }

    @Test
    fun `After success, next frame is ignored but closed`() {
        val img1 = imageReturning()
        coEvery { interactor.importQrCodeConfig(any()) } returns ImportResult.Success

        vm.onAnalyzeFrame(img1)
        dispatcher.scheduler.advanceUntilIdle()

        verify { img1.close() }
        coVerify(exactly = 1) { interactor.importQrCodeConfig(any()) }

        val img2 = imageReturning()
        vm.onAnalyzeFrame(img2)
        verify { img2.close() }

        coVerify(exactly = 1) { interactor.importQrCodeConfig(any()) }
    }

    @Test
    fun `Empty - sets flags false,false and closes image`() {
        val image = imageReturning()
        coEvery { interactor.importQrCodeConfig(any()) } returns ImportResult.Empty

        vm.onAnalyzeFrame(image)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value).isEqualTo(QrCodeScreenState(configFound = false, error = false))
        verify { image.close() }
        coVerify(exactly = 1) { interactor.importQrCodeConfig(any()) }
    }

    @Test
    fun `Error - sets error true and closes image`() {
        val image = imageReturning()
        coEvery { interactor.importQrCodeConfig(any()) } returns ImportResult.Error

        vm.onAnalyzeFrame(image)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value).isEqualTo(QrCodeScreenState(configFound = false, error = true))
        verify { image.close() }
        coVerify(exactly = 1) { interactor.importQrCodeConfig(any()) }
    }

    @Test
    fun `ResetState clears flags`() {
        val img = imageReturning()
        coEvery { interactor.importQrCodeConfig(any()) } returns ImportResult.Success

        vm.onAnalyzeFrame(img)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value).isEqualTo(QrCodeScreenState(configFound = true, error = false))

        vm.onIntent(QrCodeScreenIntent.ResetState)
        assertThat(vm.state.value).isEqualTo(QrCodeScreenState(configFound = false, error = false))
    }

    private fun frame() = FrameData(
        bytes = byteArrayOf(1, 2, 3),
        width = 10,
        height = 10,
        rotationDegrees = 0,
        imageFormat = 0
    )

    private fun imageReturning(f: FrameData = frame()): ImageProxy {
        val image = mockk<ImageProxy>(relaxed = true)
        every { image.toFrameData() } returns f
        return image
    }
}
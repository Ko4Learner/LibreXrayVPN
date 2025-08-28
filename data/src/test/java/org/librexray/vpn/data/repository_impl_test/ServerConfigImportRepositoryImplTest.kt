package org.librexray.vpn.data.repository_impl_test

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import android.graphics.ImageFormat
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.librexray.vpn.data.repository_impl.ServerConfigImportRepositoryImpl
import org.librexray.vpn.domain.interfaces.KeyValueStorage
import org.librexray.vpn.domain.interfaces.repository.ServerConfigImportRepository
import org.librexray.vpn.domain.models.FrameData
import org.librexray.vpn.domain.models.ImportResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.librexray.vpn.data.protocol_parsers.ShadowsocksParser
import org.librexray.vpn.data.protocol_parsers.SocksParser
import org.librexray.vpn.data.protocol_parsers.TrojanParser
import org.librexray.vpn.data.protocol_parsers.VlessParser
import org.librexray.vpn.data.protocol_parsers.VmessParser
import org.librexray.vpn.data.protocol_parsers.WireguardParser
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServerConfigImportRepositoryImplTest {
    private val storage = mockk<KeyValueStorage>(relaxed = true)
    private val shadowsocks = mockk<ShadowsocksParser>()
    private val socks = mockk<SocksParser>()
    private val trojan = mockk<TrojanParser>()
    private val vless = mockk<VlessParser>()
    private val vmess = mockk<VmessParser>()
    private val wireguard = mockk<WireguardParser>()

    private lateinit var ctx: Context
    private lateinit var repo: ServerConfigImportRepository

    @Before
    fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        repo = ServerConfigImportRepositoryImpl(
            storage, shadowsocks, socks, trojan, vless, vmess, wireguard, ctx
        )
    }

    @After
    fun tearDown() {
        runCatching { unmockkStatic(BarcodeScanning::class) }
        runCatching { unmockkStatic(InputImage::class) }
    }

    private fun frame(): FrameData = FrameData(
        bytes = ByteArray(24),
        width = 4,
        height = 4,
        rotationDegrees = 0,
        imageFormat = ImageFormat.NV21
    )

    private fun stubInputImageFactory() {
        mockkStatic(InputImage::class)
        val fakeImage = mockk<InputImage>(relaxed = true)
        every {
            InputImage.fromByteArray(
                any(), any(), any(),
                any(), any()
            )
        } returns fakeImage
    }

    @Test
    fun `clipboard - Empty when no text`() = runTest {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("label", ""))

        val result = repo.importFromClipboard()

        Assert.assertEquals(ImportResult.Empty, result)
        verify { storage wasNot Called }
    }

    @Test
    fun `clipboard - Success when vmess line`() = runTest {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val line = "vmess://EXAMPLE"
        cm.setPrimaryClip(ClipData.newPlainText("srv", line))

        every { vmess.parse(line) } returns mockk(relaxed = true)
        every { storage.encodeServerConfig(any(), any()) } returns "guid-1"

        val result = repo.importFromClipboard()

        Assert.assertEquals(ImportResult.Success, result)
        verify(exactly = 1) { vmess.parse(line) }
        verify(exactly = 1) { storage.encodeServerConfig(any(), any()) }
        verify(exactly = 1) { storage.setSelectedServer("guid-1") }
    }

    @Test
    fun `qr - Empty when no barcodes`() = runTest {
        stubInputImageFactory()
        mockkStatic(BarcodeScanning::class)
        val scanner = mockk<BarcodeScanner>()
        every { BarcodeScanning.getClient() } returns scanner
        every { scanner.process(any<InputImage>()) } returns
                Tasks.forResult<List<Barcode>>(emptyList())
        every { scanner.close() } returns Unit

        val result = repo.importFromQrFrame(frame())

        Assert.assertEquals(ImportResult.Empty, result)

        verify { scanner.process(any<InputImage>()) }
        verify(exactly = 1) { scanner.close() }

        verify { storage wasNot Called }
        confirmVerified(storage)
    }

    @Test
    fun `qr - Success when barcode has vmess link`() = runTest {
        stubInputImageFactory()
        mockkStatic(BarcodeScanning::class)
        val scanner = mockk<BarcodeScanner>()
        every { BarcodeScanning.getClient() } returns scanner

        val barcode = mockk<Barcode>()
        every { barcode.rawValue } returns "vmess://EXAMPLE"

        every { scanner.process(any<InputImage>()) } returns
                Tasks.forResult<List<Barcode>>(listOf(barcode))
        every { scanner.close() } returns Unit

        every { vmess.parse("vmess://EXAMPLE") } returns mockk(relaxed = true)
        every { storage.encodeServerConfig(any(), any()) } returns "guid-2"

        val result = repo.importFromQrFrame(frame())

        Assert.assertEquals(ImportResult.Success, result)

        verify(exactly = 1) { vmess.parse("vmess://EXAMPLE") }
        verify(exactly = 1) { storage.encodeServerConfig(any(), any()) }
        verify(exactly = 1) { storage.setSelectedServer("guid-2") }

        verify(exactly = 1) { scanner.close() }
        confirmVerified(storage, vmess)
    }

    @Test
    fun `qr - MLKit exception Error`() = runTest {
        stubInputImageFactory()
        mockkStatic(BarcodeScanning::class)
        val scanner = mockk<BarcodeScanner>()
        every { BarcodeScanning.getClient() } returns scanner
        every { scanner.process(any<InputImage>()) } returns
                Tasks.forException<List<Barcode>>(
                    RuntimeException("boom")
                )
        every { scanner.close() } returns Unit

        val result = repo.importFromQrFrame(frame())

        Assert.assertEquals(ImportResult.Error, result)

        verify(exactly = 1) { scanner.close() }

        verify { storage wasNot Called }
        confirmVerified(storage)
    }
}
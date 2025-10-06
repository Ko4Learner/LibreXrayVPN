package org.librexray.vpn.presentation.mapper_test

import android.graphics.ImageFormat
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Test
import org.librexray.vpn.presentation.mapper.toFrameData
import java.nio.ByteBuffer

class ImageProxyMapperTest {
    @Test
    fun `maps valid YUV_420_888 image to FrameData`() {
        val yBytes = byteArrayOf(1, 2)
        val uBytes = byteArrayOf(3, 4)
        val vBytes = byteArrayOf(5, 6)

        val yBuf = ByteBuffer.wrap(yBytes)
        val uBuf = ByteBuffer.wrap(uBytes)
        val vBuf = ByteBuffer.wrap(vBytes)

        val image = mockk<ImageProxy>(relaxed = true)
        val planeY = mockk<ImageProxy.PlaneProxy>()
        val planeU = mockk<ImageProxy.PlaneProxy>()
        val planeV = mockk<ImageProxy.PlaneProxy>()
        val info = mockk<ImageInfo>()

        every { planeY.buffer } returns yBuf
        every { planeU.buffer } returns uBuf
        every { planeV.buffer } returns vBuf
        every { image.planes } returns arrayOf(planeY, planeU, planeV)
        every { image.format } returns ImageFormat.YUV_420_888

        every { image.width } returns 640
        every { image.height } returns 480
        every { info.rotationDegrees } returns 45
        every { image.imageInfo } returns info

        val frame = image.toFrameData()

        assertThat(frame.width).isEqualTo(640)
        assertThat(frame.height).isEqualTo(480)
        assertThat(frame.rotationDegrees).isEqualTo(45)
        assertThat(frame.bytes).isNotEmpty()
    }
}
package org.librexray.vpn.presentation.mapper

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import org.librexray.vpn.domain.models.FrameData

fun ImageProxy.toFrameData(): FrameData {
    if (format != ImageFormat.YUV_420_888) {
        close()
        throw IllegalArgumentException("Unsupported image format: $format")
    }

    val nv21 = yuv420888ToNv21(this)

    return FrameData(
        bytes = nv21,
        width = width,
        height = height,
        rotationDegrees = imageInfo.rotationDegrees,
        imageFormat = InputImage.IMAGE_FORMAT_NV21
    )
}
fun yuv420888ToNv21(image: ImageProxy): ByteArray {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)

    var uvIndex = ySize
    while (vBuffer.hasRemaining() && uBuffer.hasRemaining()) {
        nv21[uvIndex++] = vBuffer.get()
        nv21[uvIndex++] = uBuffer.get()
    }
    return nv21
}
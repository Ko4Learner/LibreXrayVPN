package org.librexray.vpn.presentation.composable_element

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * CameraX preview used for scanning QR codes.
 *
 * This composable hosts a [PreviewView] inside an [AndroidView] and binds
 * both a preview and an image analysis pipeline to the current [LifecycleOwner].
 *
 * Each camera frame is passed to [onFrame] for further processing
 * (e.g., QR decoding). The callback is executed on the main executor.
 *
 * Responsibilities:
 * - Manages CameraX lifecycle automatically with Compose lifecycle.
 * - Uses [ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST] to avoid frame backlog.
 */
@Composable
fun QrCameraPreview(
    modifier: Modifier = Modifier,
    onFrame: (ImageProxy) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    onFrame(imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
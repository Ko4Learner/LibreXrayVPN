package org.librexray.vpn.presentation.composable_elements

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ScanMask(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val t by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val windowWidthPx = with(density) { 240.dp.toPx() }
        val windowHeightPx = windowWidthPx
        val cornerRadiusPx = with(density) { 24.dp.toPx() }

        val left = (size.width - windowWidthPx) / 2f
        val top = (size.height - windowHeightPx) / 2f
        val window = Rect(left, top, left + windowWidthPx, top + windowHeightPx)

        //Двигающаяся шторка внутри окна
        val innerClipPath = Path().apply {
            addRoundRect(RoundRect(window, CornerRadius(cornerRadiusPx, cornerRadiusPx)))
        }
        val bandHeightPx = windowHeightPx * 0.35f
        val bandTopPx = androidx.compose.ui.util.lerp(
            window.bottom - bandHeightPx, window.top, t
        )

        clipPath(innerClipPath) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Transparent,
                        0.30f to Color.White.copy(alpha = 0.3f),
                        0.50f to Color.White.copy(alpha = 0.7f),
                        0.70f to Color.White.copy(alpha = 0.3f),
                        1.00f to Color.Transparent
                    ),
                    startY = bandTopPx,
                    endY = bandTopPx + bandHeightPx
                ),
                topLeft = Offset(window.left, bandTopPx),
                size = Size(windowWidthPx, bandHeightPx)
            )
        }

        //Внешняя маска
        val outerMaskPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(RoundRect(window, CornerRadius(cornerRadiusPx, cornerRadiusPx)))
        }
        drawPath(outerMaskPath, color = Color.Black.copy(alpha = 0.6f))

        val strokeWidthPx = 4.dp.toPx()
        val whiskerLengthPx = 36.dp.toPx()
        val arcSweepDegrees = 90f
        val frameStroke = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val cornerOvals = arrayOf(
            Rect(
                window.left,
                window.top,
                window.left + 2 * cornerRadiusPx,
                window.top + 2 * cornerRadiusPx
            ), // TL
            Rect(
                window.right - 2 * cornerRadiusPx,
                window.top,
                window.right,
                window.top + 2 * cornerRadiusPx
            ), // TR
            Rect(
                window.right - 2 * cornerRadiusPx,
                window.bottom - 2 * cornerRadiusPx,
                window.right,
                window.bottom
            ),                  // BR
            Rect(
                window.left,
                window.bottom - 2 * cornerRadiusPx,
                window.left + 2 * cornerRadiusPx,
                window.bottom
            )                    // BL
        )
        val arcStartAngles = floatArrayOf(180f, 270f, 0f, 90f)

        val tangentHX = floatArrayOf(
            window.left + cornerRadiusPx,
            window.right - cornerRadiusPx,
            window.right - cornerRadiusPx,
            window.left + cornerRadiusPx
        )
        val tangentHY = floatArrayOf(window.top, window.top, window.bottom, window.bottom)
        val horizontalDir = floatArrayOf(+1f, -1f, -1f, +1f)

        val tangentVX = floatArrayOf(window.left, window.right, window.right, window.left)
        val tangentVY = floatArrayOf(
            window.top + cornerRadiusPx,
            window.top + cornerRadiusPx,
            window.bottom - cornerRadiusPx,
            window.bottom - cornerRadiusPx
        )
        val verticalDir = floatArrayOf(+1f, +1f, -1f, -1f)

        val cornerFramePath = Path().apply {
            for (i in 0..3) {
                moveTo(tangentHX[i], tangentHY[i])
                lineTo(tangentHX[i] + horizontalDir[i] * whiskerLengthPx, tangentHY[i])

                moveTo(tangentHX[i], tangentHY[i])
                arcTo(cornerOvals[i], arcStartAngles[i], arcSweepDegrees, true)

                moveTo(tangentVX[i], tangentVY[i])
                lineTo(tangentVX[i], tangentVY[i] + verticalDir[i] * whiskerLengthPx)
            }
        }
        drawPath(path = cornerFramePath, color = Color.White, style = frameStroke)
    }
}
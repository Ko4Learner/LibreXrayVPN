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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Composable
fun ScanMask(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val t by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = with(density) { 240.dp.toPx() }
        val height = width
        val radius = with(density) { 24.dp.toPx() }

        val left = (size.width - width) / 2f
        val top = (size.height - height) / 2f
        val window = Rect(left, top, left + width, top + height)

        val innerPath = Path().apply {
            addRoundRect(RoundRect(window, CornerRadius(radius, radius)))
        }
        val bandHeight = height * 0.4f
        val bandTop = lerp(window.bottom - bandHeight, window.top, t)

        clipPath(innerPath) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Transparent,
                        0.30f to Color(0x4DFFFFFF),
                        0.50f to Color.White.copy(alpha = 0.8f),
                        0.70f to Color(0x4DFFFFFF),
                        1.00f to Color.Transparent
                    ),
                    startY = bandTop,
                    endY = bandTop + bandHeight
                ),
                topLeft = Offset(window.left, bandTop),
                size = Size(width, bandHeight)
            )
        }

        val mask = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(RoundRect(window, CornerRadius(radius, radius)))
        }
        drawPath(mask, color = Color.Black.copy(alpha = 0.6f))

        val stroke = 4.dp.toPx()
        val style = Stroke(width = stroke, cap = StrokeCap.Round)
        val len = 36.dp.toPx()
        val sweep = 90f

        val ovalTL =
            Rect(window.left, window.top, window.left + 2 * radius, window.top + 2 * radius)
        val ovalTR =
            Rect(window.right - 2 * radius, window.top, window.right, window.top + 2 * radius)
        val ovalBR =
            Rect(window.right - 2 * radius, window.bottom - 2 * radius, window.right, window.bottom)
        val ovalBL =
            Rect(window.left, window.bottom - 2 * radius, window.left + 2 * radius, window.bottom)

        drawArc(
            Color.White, startAngle = 180f, sweepAngle = sweep, useCenter = false,
            topLeft = ovalTL.topLeft, size = ovalTL.size, style = style
        ) // левый верх
        drawArc(
            Color.White, startAngle = 270f, sweepAngle = sweep, useCenter = false,
            topLeft = ovalTR.topLeft, size = ovalTR.size, style = style
        ) // правый верх
        drawArc(
            Color.White, startAngle = 0f, sweepAngle = sweep, useCenter = false,
            topLeft = ovalBR.topLeft, size = ovalBR.size, style = style
        ) // правый низ
        drawArc(
            Color.White, startAngle = 90f, sweepAngle = sweep, useCenter = false,
            topLeft = ovalBL.topLeft, size = ovalBL.size, style = style
        ) // левый низ

        drawLine(
            Color.White,
            Offset(window.left + radius, window.top),
            Offset(window.left + radius + len, window.top),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )   // верх, слева направо
        drawLine(
            Color.White,
            Offset(window.right - radius - len, window.top),
            Offset(window.right - radius, window.top),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        ) // верх, справа налево

        drawLine(
            Color.White,
            Offset(window.left, window.top + radius),
            Offset(window.left, window.top + radius + len),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )   // слева, сверху вниз
        drawLine(
            Color.White,
            Offset(window.right, window.top + radius),
            Offset(window.right, window.top + radius + len),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        ) // справа, сверху вниз

        drawLine(
            Color.White,
            Offset(window.left + radius, window.bottom),
            Offset(window.left + radius + len, window.bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )   // низ, слева направо
        drawLine(
            Color.White,
            Offset(window.right - radius - len, window.bottom),
            Offset(window.right - radius, window.bottom),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        ) // низ, справа налево

        drawLine(
            Color.White,
            Offset(window.left, window.bottom - radius - len),
            Offset(window.left, window.bottom - radius),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )   // слева, снизу вверх
        drawLine(
            Color.White,
            Offset(window.right, window.bottom - radius - len),
            Offset(window.right, window.bottom - radius),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        ) // справа, снизу вверх
    }
}
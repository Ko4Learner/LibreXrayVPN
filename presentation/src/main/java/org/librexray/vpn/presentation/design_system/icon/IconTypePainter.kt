package org.librexray.vpn.presentation.design_system.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

/**
 * Creates a [Painter] for the given [IconType], memoized across recompositions.
 *
 * Guarantees:
 * - Always returns a valid painter, falling back to [Icons.Default.Image]
 *   if the resource cannot be loaded.
 * - Safe to call from any @Composable scope.
 */
@Composable
fun IconType.rememberPainter():Painter = when(this){
    is IconType.Vector -> rememberVectorPainter(image = image)
    is IconType.Drawable -> runCatching {
        painterResource(id = image)
    }.getOrElse {
        rememberVectorPainter(Icons.Default.Image)
    }
}
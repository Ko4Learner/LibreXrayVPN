package org.librexray.vpn.presentation.design_system.icon

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Abstraction over different icon sources.
 *
 * UI code should depend only on [IconType] rather than the underlying
 * implementation (vector vs drawable) to remain platform-agnostic.
 */
sealed interface IconType {
    data class Vector(val image: ImageVector) : IconType
    data class Drawable(@DrawableRes val image: Int) : IconType
}
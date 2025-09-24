package org.librexray.vpn.presentation.design_system.icon

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface IconType {
    data class Vector(val image: ImageVector) : IconType
    data class Drawable(@DrawableRes val image: Int) : IconType
}
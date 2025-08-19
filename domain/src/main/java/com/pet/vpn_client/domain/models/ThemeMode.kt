package com.pet.vpn_client.domain.models

import java.util.Locale

/**
 * UI theme mode for the application.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    /**
     * Returns a stable string tag for persistence.
     *
     * @return "system", "light" or "dark".
     */
    fun toTag(): String = when (this) {
        SYSTEM -> SYSTEM_TAG
        LIGHT -> LIGHT_TAG
        DARK -> DARK_TAG
    }

    companion object {
        /**
         * Parses a theme mode from a string tag.
         *
         * Matching is case-insensitive. Unknown or null values return [SYSTEM].
         */
        fun fromTag(tag: String?): ThemeMode {
            return when (tag?.lowercase(Locale.ROOT)) {
                LIGHT_TAG -> LIGHT
                DARK_TAG -> DARK
                else -> SYSTEM
            }
        }
        private const val SYSTEM_TAG = "system"
        private const val LIGHT_TAG = "light"
        private const val DARK_TAG = "dark"
    }
}
package org.librexray.vpn.domain.models

import org.librexray.vpn.coreandroid.utils.Constants
import java.util.Locale

/**
 * Application locale selection.
 */
enum class AppLocale {
    SYSTEM,
    RU,
    EN;

    /**
     * Returns the persistent tag for this locale choice.
     *
     * @return "", "ru" or "en".
     */
    fun toTag(): String = when (this) {
        SYSTEM -> Constants.SYSTEM_LOCALE_TAG
        RU -> Constants.RU_LOCALE_TAG
        EN -> Constants.EN_LOCALE_TAG
    }

    companion object {
        /**
         * Parses an app locale from a tag.
         *
         * Matching is case-insensitive. Unknown or null values
         * return [SYSTEM].
         */
        fun fromTag(tag: String?): AppLocale = when (tag?.lowercase(Locale.ROOT)) {
            Constants.RU_LOCALE_TAG -> RU
            Constants.EN_LOCALE_TAG -> EN
            else -> SYSTEM
        }
    }
}
package com.pet.vpn_client.core.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Utility object for updating the application locale dynamically.
 */
object LocaleHelper {
    /**
     * Updates the locale of the provided [context] and returns a new [Context]
     * with the updated configuration.
     *
     * @param context The original context.
     * @param locale The target locale to set.
     * @return A new context with the updated locale.
     */
    fun updateLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
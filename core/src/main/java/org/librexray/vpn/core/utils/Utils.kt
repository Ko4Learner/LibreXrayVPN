/* SPDX-License-Identifier: GPL-3.0-or-later */
/*
 * This file is part of LibreXrayVPN.
 *
 * Modified from v2rayNG (https://github.com/2dust/v2rayNG).
 */

package org.librexray.vpn.core.utils

import android.util.Base64
import android.util.Patterns
import android.webkit.URLUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder

/**
 * Utility object containing helper methods for working with
 * IP addresses, URLs, Base64 encoding/decoding, and JSON parsing.
 */
object Utils {
    private val IPV4_REGEX =
        Regex("^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}$")
    private val IPV6_REGEX = Regex(
        "^([0-9A-Fa-f]{1,4})?(:[0-9A-Fa-f]{1,4})*::([0-9A-Fa-f]{1,4})?(:[0-9A-Fa-f]{1,4})*|([0-9A-Fa-f]{1,4})(:[0-9A-Fa-f]{1,4}){7}$"
    )

    /**
     * Global error handler callback.
     *
     * - Default implementation does nothing, which allows
     *   running unit tests without Android dependencies.
     * - In production, this is reassigned in MyApp to log errors via Log.e.
     */
    @Volatile
    var error: (String, Throwable?) -> Unit = { _, _ -> }

    /**
     * Attempts to decode a Base64-encoded [text].
     * Tries both standard and URL-safe Base64.
     * If decoding fails, returns the original string or an empty string if null.
     */
    fun decode(text: String?): String {
        return tryDecodeBase64(text) ?: text?.trimEnd('=')?.let { tryDecodeBase64(it) }.orEmpty()
    }

    /**
     * Attempts to decode a Base64 string in both standard and URL-safe formats.
     * Returns null if decoding fails.
     */
    fun tryDecodeBase64(text: String?): String? {
        if (text.isNullOrEmpty()) return null

        try {
            return Base64.decode(text, Base64.NO_WRAP).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            error("Failed to decode standard base64", e)
        }
        try {
            return Base64.decode(text, Base64.NO_WRAP.or(Base64.URL_SAFE)).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            error( "Failed to decode URL-safe base64", e)
        }
        return null
    }

    /**
     * Validates whether [value] is a valid IPv4 or IPv6 address.
     * Supports CIDR notation and IPv4-mapped IPv6 addresses.
     */
    fun isIpAddress(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false
        try {
            var addr = value.trim()
            if (addr.isEmpty()) return false

            if (addr.contains("/")) {
                val arr = addr.split("/")
                if (arr.size == 2 && arr[1].toIntOrNull() != null && arr[1].toInt() > -1) {
                    addr = arr[0]
                }
            }

            if (addr.startsWith("::ffff:") && '.' in addr) {
                addr = addr.drop(7)
            } else if (addr.startsWith("[::ffff:") && '.' in addr) {
                addr = addr.drop(8).replace("]", "")
            }

            val octets = addr.split('.')
            if (octets.size == 4) {
                if (octets[3].contains(":")) {
                    addr = addr.substring(0, addr.indexOf(":"))
                }
                return isIpv4Address(addr)
            }

            return isIpv6Address(addr)
        } catch (e: Exception) {
            error("Failed to validate IP address", e)
            return false
        }
    }

    /**
     * Checks if [value] is strictly a valid IPv4 or IPv6 address without any CIDR notation.
     */
    fun isPureIpAddress(value: String): Boolean {
        return isIpv4Address(value) || isIpv6Address(value)
    }

    /**
     * Validates whether [value] is a correct URL or domain name.
     */
    fun isValidUrl(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false

        return try {
            Patterns.WEB_URL.matcher(value).matches() ||
                    Patterns.DOMAIN_NAME.matcher(value).matches() ||
                    URLUtil.isValidUrl(value)
        } catch (e: Exception) {
            error("Failed to validate URL", e)
            false
        }
    }

    /**
     * Decodes a percent-encoded URL string using UTF-8.
     * Returns the original string if decoding fails.
     */
    fun urlDecode(url: String): String {
        return try {
            URLDecoder.decode(url, Charsets.UTF_8.toString())
        } catch (e: Exception) {
            error("Failed to decode URL", e)
            url
        }
    }

    /**
     * Wraps [address] in square brackets if it is a valid IPv6 address and does not already contain them.
     */
    fun getIpv6Address(address: String?): String {
        if (address.isNullOrEmpty()) return ""

        return if (isIpv6Address(address) && !address.contains('[') && !address.contains(']')) {
            "[$address]"
        } else {
            address
        }
    }

    /**
     * Replaces spaces and illegal characters (e.g., '|') in [str] with their URL-encoded equivalents.
     */
    fun fixIllegalUrl(str: String): String {
        return str.replace(" ", "%20")
            .replace("|", "%7C")
    }

    /**
     * Inline Gson extension function for parsing JSON into a reified type [T].
     */
    inline fun <reified T> Gson.fromJsonReified(json: String): T =
        fromJson(json, object : TypeToken<T>() {}.type)

    // --- Internal IPv4validation method ---
    private fun isIpv4Address(value: String): Boolean {
        return IPV4_REGEX.matches(value)
    }

    // --- Internal IPv6 validation method ---
    private fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.startsWith("[") && addr.endsWith("]")) {
            addr = addr.drop(1).dropLast(1)
        }
        return IPV6_REGEX.matches(addr)
    }
}

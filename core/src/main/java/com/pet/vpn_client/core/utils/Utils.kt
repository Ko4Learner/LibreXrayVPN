package com.pet.vpn_client.core.utils

import android.content.Context
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import java.net.URLDecoder
import java.net.URLEncoder


object Utils {
    private val IPV4_REGEX =
        Regex("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$")
    private val IPV6_REGEX = Regex("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$")

    /**
     * Parse a string to an integer with a default value.
     *
     * @param str The string to parse.
     * @param default The default value if parsing fails.
     * @return The parsed integer, or the default value if parsing fails.
     */
    fun parseInt(str: String?, default: Int = 0): Int {
        return str?.toIntOrNull() ?: default
    }

    /**
     * Decode a base64 encoded string.
     *
     * @param text The base64 encoded string.
     * @return The decoded string, or an empty string if decoding fails.
     */
    fun decode(text: String?): String {
        return tryDecodeBase64(text) ?: text?.trimEnd('=')?.let { tryDecodeBase64(it) }.orEmpty()
    }

    /**
     * Try to decode a base64 encoded string.
     *
     * @param text The base64 encoded string.
     * @return The decoded string, or null if decoding fails.
     */
    fun tryDecodeBase64(text: String?): String? {
        if (text.isNullOrEmpty()) return null

        try {
            return Base64.decode(text, Base64.NO_WRAP).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to decode standard base64", e)
        }
        try {
            return Base64.decode(text, Base64.NO_WRAP.or(Base64.URL_SAFE)).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to decode URL-safe base64", e)
        }
        return null
    }

    /**
     * Encode a string to base64.
     *
     * @param text The string to encode.
     * @return The base64 encoded string, or an empty string if encoding fails.
     */
    fun encode(text: String): String {
        return try {
            Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to encode text to base64", e)
            ""
        }
    }

    /**
     * Check if a string is a valid IP address.
     *
     * @param value The string to check.
     * @return True if the string is a valid IP address, false otherwise.
     */
    fun isIpAddress(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false

        try {
            var addr = value.trim()
            if (addr.isEmpty()) return false

            //CIDR
            if (addr.contains("/")) {
                val arr = addr.split("/")
                if (arr.size == 2 && arr[1].toIntOrNull() != null && arr[1].toInt() > -1) {
                    addr = arr[0]
                }
            }

            // Handle IPv4-mapped IPv6 addresses
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
            Log.e(Constants.TAG, "Failed to validate IP address", e)
            return false
        }
    }

    /**
     * Check if a string is a pure IP address (IPv4 or IPv6).
     *
     * @param value The string to check.
     * @return True if the string is a pure IP address, false otherwise.
     */
    fun isPureIpAddress(value: String): Boolean {
        return isIpv4Address(value) || isIpv6Address(value)
    }

    /**
     * Check if a string is a valid IPv4 address.
     *
     * @param value The string to check.
     * @return True if the string is a valid IPv4 address, false otherwise.
     */
    private fun isIpv4Address(value: String): Boolean {
        return IPV4_REGEX.matches(value)
    }

    /**
     * Check if a string is a valid IPv6 address.
     *
     * @param value The string to check.
     * @return True if the string is a valid IPv6 address, false otherwise.
     */
    private fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.startsWith("[") && addr.endsWith("]")) {
            addr = addr.drop(1).dropLast(1)
        }
        return IPV6_REGEX.matches(addr)
    }

    /**
     * Check if a string is a valid URL.
     *
     * @param value The string to check.
     * @return True if the string is a valid URL, false otherwise.
     */
    fun isValidUrl(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false

        return try {
            Patterns.WEB_URL.matcher(value).matches() ||
                    Patterns.DOMAIN_NAME.matcher(value).matches() ||
                    URLUtil.isValidUrl(value)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to validate URL", e)
            false
        }
    }

    /**
     * Decode a URL-encoded string.
     *
     * @param url The URL-encoded string.
     * @return The decoded string, or the original string if decoding fails.
     */
    fun urlDecode(url: String): String {
        return try {
            URLDecoder.decode(url, Charsets.UTF_8.toString())
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to decode URL", e)
            url
        }
    }

    /**
     * Encode a string to URL-encoded format.
     *
     * @param url The string to encode.
     * @return The URL-encoded string, or the original string if encoding fails.
     */
    fun urlEncode(url: String): String {
        return try {
            URLEncoder.encode(url, Charsets.UTF_8.toString()).replace("+", "%20")
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to encode URL", e)
            url
        }
    }

    /**
     * Read text from an asset file.
     *
     * @param context The context to use.
     * @param fileName The name of the asset file.
     * @return The content of the asset file as a string.
     */
    fun readTextFromAssets(context: Context?, fileName: String): String {
        if (context == null) return ""

        return try {
            context.assets.open(fileName).use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to read asset file: $fileName", e)
            ""
        }
    }

    /**
     * Get the path to the user asset directory.
     *
     * @param context The context to use.
     * @return The path to the user asset directory.
     */
    fun userAssetPath(context: Context?): String {
        if (context == null) return ""

        return try {
            context.getExternalFilesDir(Constants.DIR_ASSETS)?.absolutePath
                ?: context.getDir(Constants.DIR_ASSETS, 0).absolutePath
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to get user asset path", e)
            ""
        }
    }

    /**
     * Get the device ID for XUDP base key.
     *
     * @return The device ID for XUDP base key.
     */
    fun getDeviceIdForXUDPBaseKey(): String {
        return try {
            val androidId = Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8)
            Base64.encodeToString(androidId.copyOf(32), Base64.NO_PADDING.or(Base64.URL_SAFE))
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to generate device ID", e)
            ""
        }
    }

    /**
     * Get the IPv6 address in a formatted string.
     *
     * @param address The IPv6 address.
     * @return The formatted IPv6 address, or the original address if not valid.
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
     * Fix illegal characters in a URL.
     *
     * @param str The URL string.
     * @return The URL string with illegal characters replaced.
     */
    fun fixIllegalUrl(str: String): String {
        return str.replace(" ", "%20")
            .replace("|", "%7C")
    }
}

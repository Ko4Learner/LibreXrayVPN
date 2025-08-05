package com.pet.vpn_client.core.utils

import android.content.Context
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import java.net.URLDecoder

object Utils {
    private val IPV4_REGEX =
        Regex("^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}$")
    private val IPV6_REGEX = Regex(
        "^([0-9A-Fa-f]{1,4})?(:[0-9A-Fa-f]{1,4})*::([0-9A-Fa-f]{1,4})?(:[0-9A-Fa-f]{1,4})*|([0-9A-Fa-f]{1,4})(:[0-9A-Fa-f]{1,4}){7}$"
    )

    fun parseInt(str: String?, default: Int = 0): Int {
        return str?.toIntOrNull() ?: default
    }

    fun decode(text: String?): String {
        return tryDecodeBase64(text) ?: text?.trimEnd('=')?.let { tryDecodeBase64(it) }.orEmpty()
    }

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

    fun isPureIpAddress(value: String): Boolean {
        return isIpv4Address(value) || isIpv6Address(value)
    }

    private fun isIpv4Address(value: String): Boolean {
        return IPV4_REGEX.matches(value)
    }

    private fun isIpv6Address(value: String): Boolean {
        var addr = value
        if (addr.startsWith("[") && addr.endsWith("]")) {
            addr = addr.drop(1).dropLast(1)
        }
        return IPV6_REGEX.matches(addr)
    }

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

    fun urlDecode(url: String): String {
        return try {
            URLDecoder.decode(url, Charsets.UTF_8.toString())
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to decode URL", e)
            url
        }
    }

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

    fun getDeviceIdForXUDPBaseKey(): String {
        return try {
            val androidId = Settings.Secure.ANDROID_ID.toByteArray(Charsets.UTF_8)
            Base64.encodeToString(androidId.copyOf(32), Base64.NO_PADDING.or(Base64.URL_SAFE))
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to generate device ID", e)
            ""
        }
    }

    fun getIpv6Address(address: String?): String {
        if (address.isNullOrEmpty()) return ""

        return if (isIpv6Address(address) && !address.contains('[') && !address.contains(']')) {
            "[$address]"
        } else {
            address
        }
    }

    fun fixIllegalUrl(str: String): String {
        return str.replace(" ", "%20")
            .replace("|", "%7C")
    }
}

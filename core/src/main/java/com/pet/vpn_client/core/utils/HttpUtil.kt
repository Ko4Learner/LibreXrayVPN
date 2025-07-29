package com.pet.vpn_client.core.utils

import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.core.utils.Utils.encode
import com.pet.vpn_client.core.utils.Utils.urlDecode
import java.io.IOException
import java.net.HttpURLConnection
import java.net.IDN
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

object HttpUtil {

    fun idnToASCII(str: String): String {
        val url = URL(str)
        val host = url.host
        val asciiHost = IDN.toASCII(url.host, IDN.ALLOW_UNASSIGNED)
        if (host != asciiHost) {
            return str.replace(host, asciiHost)
        } else {
            return str
        }
    }

    fun resolveHostToIP(host: String, ipv6Preferred: Boolean = false): List<String>? {
        try {
            // If it's already an IP address, return it as a list
            if (Utils.isPureIpAddress(host)) {
                return null
            }

            // Get all IP addresses
            val addresses = InetAddress.getAllByName(host)
            if (addresses.isEmpty()) {
                return null
            }

            // Sort addresses based on preference
            val sortedAddresses = if (ipv6Preferred) {
                addresses.sortedWith(compareByDescending { it is Inet6Address })
            } else {
                addresses.sortedWith(compareBy { it is Inet6Address })
            }

            val ipList = sortedAddresses.mapNotNull { it.hostAddress }

            Log.i(Constants.TAG, "Resolved IPs for $host: ${ipList.joinToString()}")

            return ipList
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to resolve host to IP", e)
            return null
        }
    }

    fun getUrlContent(url: String, timeout: Int, httpPort: Int = 0): String? {
        val conn = createProxyConnection(url, httpPort, timeout, timeout) ?: return null
        try {
            return conn.inputStream.bufferedReader().readText()
        } catch (_: Exception) {
        } finally {
            conn.disconnect()
        }
        return null
    }

    @Throws(IOException::class)
    fun getUrlContentWithUserAgent(url: String?, timeout: Int = 15000, httpPort: Int = 0): String {
        var currentUrl = url
        var redirects = 0
        val maxRedirects = 3

        while (redirects++ < maxRedirects) {
            if (currentUrl == null) continue
            val conn = createProxyConnection(currentUrl, httpPort, timeout, timeout) ?: continue
            //TODO
            //conn.setRequestProperty("User-agent", "v2rayNG/${BuildConfig.VERSION_NAME}")
            conn.connect()

            val responseCode = conn.responseCode
            when (responseCode) {
                in 300..399 -> {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location.isNullOrEmpty()) {
                        throw IOException("Redirect location not found")
                    }
                    currentUrl = location
                    continue
                }

                else -> try {
                    return conn.inputStream.use { it.bufferedReader().readText() }
                } finally {
                    conn.disconnect()
                }
            }
        }
        throw IOException("Too many redirects")
    }

    fun createProxyConnection(
        urlStr: String,
        port: Int,
        connectTimeout: Int = 15000,
        readTimeout: Int = 15000,
        needStream: Boolean = false
    ): HttpURLConnection? {

        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            // Create a connection
            conn = if (port == 0) {
                url.openConnection()
            } else {
                url.openConnection(
                    Proxy(
                        Proxy.Type.HTTP,
                        InetSocketAddress(Constants.LOOPBACK, port)
                    )
                )
            } as HttpURLConnection

            // Set connection and read timeouts
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            if (!needStream) {
                // Set request headers
                conn.setRequestProperty("Connection", "close")
                // Disable automatic redirects
                conn.instanceFollowRedirects = false
                // Disable caching
                conn.useCaches = false
            }

            //Add Basic Authorization
            url.userInfo?.let {
                conn.setRequestProperty(
                    "Authorization",
                    "Basic ${encode(urlDecode(it))}"
                )
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to create proxy connection", e)
            // If an exception occurs, close the connection and return null
            conn?.disconnect()
            return null
        }
        return conn
    }
}
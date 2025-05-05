package com.pet.vpn_client.data.dto

data class ServerTestDelayInfo(var testDelayMillis: Long = 0L) {
    fun getTestDelayString(): String {
        if (testDelayMillis == 0L) {
            return ""
        }
        return testDelayMillis.toString() + "ms"
    }
}
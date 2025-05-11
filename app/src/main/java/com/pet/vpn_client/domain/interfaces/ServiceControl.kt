package com.pet.vpn_client.domain.interfaces

import android.app.Service

interface ServiceControl {

    fun getService(): Service

    fun startService()

    fun stopService()

    fun vpnProtect(socket: Int): Boolean
}
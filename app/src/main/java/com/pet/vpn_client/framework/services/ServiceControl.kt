package com.pet.vpn_client.framework.services

import android.app.Service

interface ServiceControl {

    fun getService(): Service

    fun startService()

    fun stopService()

    fun vpnProtect(socket: Int): Boolean
}
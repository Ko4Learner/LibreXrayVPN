package com.pet.vpn_client.data

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class MyVpnService() : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d("MyVpnService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyVpnService", "onStartCommand")
        isRunning = true
        startVpn()
        return START_STICKY
    }

    override fun onRevoke() {
        Log.d("MyVpnService", "onRevoke")
        stopVpn()
    }

    override fun onDestroy() {
        Log.d("MyVpnService", "onDestroy")
        stopVpn()
    }

    private fun startVpn() {
        val builder = Builder()
            .addAddress("10.0.0.2", 32) // Пример IP и префикса
            .addDnsServer("8.8.8.8") // Пример DNS-сервера
            .addRoute("0.0.0.0", 0) // Весь трафик через VPN
            .setMtu(1500) // Пример MTU
            .setSession("MyVpnService")

        vpnInterface = try {
            builder.establish()
        } catch (e: Exception) {
            Log.e("MyVpnService", "Failed to establish VPN interface", e)
            return
        }

        if (vpnInterface == null) {
            Log.e("MyVpnService", "VPN interface is null")
            return
        }

        val fileDescriptor = vpnInterface?.fileDescriptor
        if (fileDescriptor == null) {
            Log.e("MyVpnService", "File descriptor is null")
            return
        }

        val input = FileInputStream(fileDescriptor)
        val output = FileOutputStream(fileDescriptor)

        Thread {
            val packet = DatagramPacket(ByteArray(1500), 1500) // Настройте размер буфера
            val serverAddress = InetSocketAddress("192.168.1.1", 5555) // Замените на адрес и порт вашего сервера

            try {
                val socket = DatagramSocket()
                while (isRunning) {
                    input.read(packet.data, 0, packet.data.size)
                    packet.address = serverAddress.address
                    packet.port = serverAddress.port
                    socket.send(packet)
                }
            } catch (e: IOException) {
                Log.e("MyVpnService", "Error in VPN input thread", e)
            }
        }.start()

        Thread {
            val serverSocket = DatagramSocket(5555)
            val receivedPacket = DatagramPacket(ByteArray(1500), 1500)

            try {
                while (isRunning) {
                    serverSocket.receive(receivedPacket)
                    output.write(receivedPacket.data)
                }
            } catch (e: IOException) {
                Log.e("MyVpnService", "Error in VPN output thread", e)
            }
        }.start()
    }

    private fun stopVpn() {
        isRunning = false
        vpnInterface?.close()
        vpnInterface = null
    }
}
package com.pet.vpn_client.framework.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.ProxyInfo
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.interfaces.ServiceControl
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.SettingsManager
import com.pet.vpn_client.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class VPNService : VpnService(), ServiceControl {

    @Inject
    lateinit var storage: KeyValueStorage

    @Inject
    lateinit var serviceManager: ServiceManager

    @Inject
    lateinit var settingsManager: SettingsManager

    private lateinit var mInterface: ParcelFileDescriptor
    private var isRunning = false
    private lateinit var process: Process

    private val connectivityManager by lazy { getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager }

    private val networkRequest by lazy {
        NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()
    }

    private val networkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setUnderlyingNetworks(arrayOf(network))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                setUnderlyingNetworks(arrayOf(network))
            }

            override fun onLost(network: Network) {
                setUnderlyingNetworks(null)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        serviceManager.setService(this)
        showNotification()
    }

    private fun showNotification() {
        val channelId = "vpn_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "VPN",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VPN")
            .setContentText("VPN сервис запущен")
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }


    override fun onRevoke() {
        stopVpn()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        //NotificationService.stopNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //super.onStartCommand(intent, flags, startId)

        if (serviceManager.startCoreLoop()) {
            startService()
        }

        return START_STICKY
    }

    override fun getService(): Service {
        return this
    }

    override fun startService() {
        setup()
    }

    override fun stopService() {
        stopVpn(true)
    }

    override fun vpnProtect(socket: Int): Boolean {
        return protect(socket)
    }

    private fun setup() {
        if (prepare(this) != null || setupVpnService() != true) {
            Log.d(Constants.TAG, "Failed to setup VPN")
            return
        }
        runTun2socks()
    }

    private fun setupVpnService(): Boolean {
        Log.d(Constants.TAG, "Start setup VPN")
        val builder = Builder()
        builder.setMtu(VPN_MTU)
            .addAddress(PRIVATE_VLAN4_CLIENT, 30)
            .addRoute(DEFAULT_ROUTE, 0)

        //нужно ли мне это?
//        settingsManager.getVpnDnsServers()
//            .forEach {
//                if (Utils.isPureIpAddress(it)) {
        builder.addDnsServer("1.1.1.1")
//                }
//            }
        builder.setSession(serviceManager.getRunningServerName())
        //TODO разобраться с запретом на использование приложением впн
//        builder.addDisallowedApplication("com.pet.vpn_client")
        try {
            if (::mInterface.isInitialized) mInterface.close()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to request network___: ${e.message}")
        }

        try {
            connectivityManager.requestNetwork(networkRequest, networkCallback)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to request network: ${e.message}")
        }
        builder.setMetered(false)
        if (storage.decodeSettingsBool(Constants.PREF_APPEND_HTTP_PROXY)) {
            builder.setHttpProxy(
                ProxyInfo.buildDirectProxy(
                    "127.0.0.1",
                    settingsManager.getHttpPort()
                )
            )
        }
        try {
            mInterface = builder.establish()!!
            Log.d(Constants.TAG, "builder.establish() returned: $mInterface")
            isRunning = true
            return true
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to establish network: ${e.message}")
            stopVpn()
        }
        return false
    }

    private fun runTun2socks() {
        val socksPort = PORT_SOCKS
        val cmd = arrayListOf(
            File(
                applicationContext.applicationInfo.nativeLibraryDir,
                TUN2SOCKS
            ).absolutePath,
            "--netif-ipaddr", PRIVATE_VLAN4_ROUTER,
            "--netif-netmask", "255.255.255.252",
            "--socks-server-addr", "127.0.0.1:${socksPort}",
            "--tunmtu", VPN_MTU.toString(),
            "--sock-path", "sock_path",
            "--enable-udprelay",
            "--loglevel", "notice"
        )

        try {
            val processBuilder = ProcessBuilder(cmd)
            processBuilder.redirectErrorStream(true)
            process = processBuilder
                .directory(applicationContext.filesDir)
                .start()
            Thread {
                process.waitFor()
                if (isRunning) {
                    runTun2socks()
                }
            }.start()
            sendFd()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to run tun2socks: ${e.message}")
        }
    }

    private fun sendFd() {
        Log.d(Constants.TAG, "Trying sendFd")
        val fd = mInterface.fileDescriptor
        val path = File(applicationContext.filesDir, "sock_path").absolutePath

        CoroutineScope(Dispatchers.IO).launch {
            var failsCount = 0
            while (true) try {
                Thread.sleep(50L shl failsCount)
                Log.d(Constants.TAG, "Trying to connect to tun2socks via $path with fd=$fd")
                LocalSocket().use { localSocket ->
                    localSocket.connect(
                        LocalSocketAddress(
                            path,
                            LocalSocketAddress.Namespace.FILESYSTEM
                        )
                    )
                    localSocket.setFileDescriptorsForSend(arrayOf(fd))
                    Log.d(Constants.TAG, "Sent fd $fd to tun2socks")
                    localSocket.outputStream.write(0)
                }
                break
            } catch (e: Exception) {
                Log.e("VPNService", "Failed to send fd: ${e.message}")
                if (++failsCount > 5) break
            }
        }
    }

    private fun stopVpn(isForcedStop: Boolean = false) {
        Log.d(Constants.TAG, "Stop service VPNService")
        isRunning = false
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
        try {
            process.destroy()
        } catch (e: Exception) {
            Log.e("VPNService", "Failed to destroy process: ${e.message}")
        }

        serviceManager.stopCoreLoop()

        if (isForcedStop) {
            stopSelf()
            try {
                mInterface.close()
            } catch (e: Exception) {
                Log.e("VPNService", "Failed to close interface: ${e.message}")
            }
        }
    }

    //    //применение локали (язык) приложения
//    override fun attachBaseContext(newBase: Context?) {
//        val context = newBase?.let {
//            MyContextWrapper.wrap(newBase, settingsManager.getLocale())
//        }
//        super.attachBaseContext(context)
//    }

    companion object {
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "10.10.14.1"
        private const val DEFAULT_ROUTE = "0.0.0.0"
        private const val PORT_SOCKS = 10808
        private const val PRIVATE_VLAN4_ROUTER = "10.10.14.2"
        private const val TUN2SOCKS = "libtun2socks.so"
    }
}
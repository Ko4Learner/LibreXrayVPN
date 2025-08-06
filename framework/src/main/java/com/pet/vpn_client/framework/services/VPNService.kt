package com.pet.vpn_client.framework.services

import android.content.Intent
import android.net.ConnectivityManager
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.util.Log
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.ServiceManager
import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.state.ServiceState
import com.pet.vpn_client.framework.notification.NotificationFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class VPNService : VpnService() {
    @Inject
    lateinit var serviceManager: ServiceManager

    @Inject
    lateinit var notificationFactory: NotificationFactory

    @Inject
    lateinit var serviceStateRepository: ServiceStateRepository

    private lateinit var mInterface: ParcelFileDescriptor
    private var isRunning = false
    private lateinit var process: Process

    //TODO необходимость connectivityManager, networkRequest, networkCallback
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
        startForeground(1, notificationFactory.createNotification("Vpn"))
    }

    //вызывается при ручном отзыве разрешения пользователем
    override fun onRevoke() {
        stopVpn()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(Constants.EXTRA_COMMAND)) {
            Constants.COMMAND_START_SERVICE -> {
                if (serviceManager.startCoreLoop()) setup()
            }

            Constants.COMMAND_STOP_SERVICE -> {
                stopVpn(true)
                serviceStateRepository.updateState(ServiceState.Stopped)
                return START_NOT_STICKY
            }

            Constants.COMMAND_RESTART_SERVICE -> {
                serviceManager.restartService()
            }
        }
        return START_STICKY
    }

    private fun setup() {
        if (prepare(this) != null || !setupVpnService()) return
        runTun2socks()
    }

    private fun setupVpnService(): Boolean {
        val builder = Builder()
        builder.setMtu(VPN_MTU)
            .addAddress(PRIVATE_VLAN4_CLIENT, 30)
            .addRoute(DEFAULT_ROUTE, 0)
            .addDnsServer(DEFAULT_DNS_SERVER)
            .setSession(serviceManager.getRunningServerName())
            .addDisallowedApplication(Constants.ANG_PACKAGE)
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
        try {
            mInterface = builder.establish()!!
            isRunning = true
            return true
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to establish network: ${e.message}")
            stopVpn()
        }
        return false
    }

    private fun runTun2socks() {
        val cmd = arrayListOf(
            File(
                applicationContext.applicationInfo.nativeLibraryDir,
                TUN2SOCKS
            ).absolutePath,
            ARG_NETIF_IPADDR, PRIVATE_VLAN4_ROUTER,
            ARG_NETIF_NETMASK, NETMASK_30,
            ARG_SOCKS_SERVER_ADDR, "$LOCALHOST:$PORT_SOCKS",
            ARG_TUN_MTU, VPN_MTU.toString(),
            ARG_SOCK_PATH, SOCK_PATH,
            ARG_ENABLE_UDP_RELAY,
            ARG_LOG_LEVEL, LOG_LEVEL_NOTICE
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
            serviceStateRepository.updateState(ServiceState.Connected)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to run tun2socks: ${e.message}")
        }
    }

    private fun sendFd() {
        val fd = mInterface.fileDescriptor
        val path = File(applicationContext.filesDir, SOCK_PATH).absolutePath

        CoroutineScope(Dispatchers.IO).launch {
            var failsCount = 0
            while (true) try {
                Thread.sleep(50L shl failsCount)
                LocalSocket().use { localSocket ->
                    localSocket.connect(
                        LocalSocketAddress(
                            path,
                            LocalSocketAddress.Namespace.FILESYSTEM
                        )
                    )
                    localSocket.setFileDescriptorsForSend(arrayOf(fd))
                    localSocket.outputStream.write(0)
                }
                break
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to send fd: ${e.message}")
                if (++failsCount > 5) break
            }
        }
    }

    private fun stopVpn(isForcedStop: Boolean = false) {
        isRunning = false
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
        try {
            process.destroy()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Failed to destroy process: ${e.message}")
        }

        serviceManager.stopCoreLoop()
        if (isForcedStop) {
            stopSelf()
            try {
                mInterface.close()
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to close interface: ${e.message}")
            }
        }
        serviceStateRepository.updateState(ServiceState.Stopped)
    }

    companion object {
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "10.10.14.1"
        private const val DEFAULT_ROUTE = "0.0.0.0"
        private const val PORT_SOCKS = 10808
        private const val PRIVATE_VLAN4_ROUTER = "10.10.14.2"
        private const val TUN2SOCKS = "libtun2socks.so"
        private const val DEFAULT_DNS_SERVER = "1.1.1.1"
        private const val ARG_NETIF_IPADDR = "--netif-ipaddr"
        private const val ARG_NETIF_NETMASK = "--netif-netmask"
        private const val NETMASK_30 = "255.255.255.252"
        private const val LOCALHOST = "127.0.0.1"
        private const val ARG_SOCKS_SERVER_ADDR = "--socks-server-addr"
        private const val ARG_TUN_MTU = "--tunmtu"
        private const val ARG_SOCK_PATH = "--sock-path"
        private const val SOCK_PATH = "sock_path"
        private const val ARG_ENABLE_UDP_RELAY = "--enable-udprelay"
        private const val ARG_LOG_LEVEL = "--loglevel"
        private const val LOG_LEVEL_NOTICE = "notice"
    }
}
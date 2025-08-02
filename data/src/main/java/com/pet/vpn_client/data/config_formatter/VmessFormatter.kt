package com.pet.vpn_client.data.config_formatter

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.ConfigManager
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.domain.models.VmessQRCode
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import com.pet.vpn_client.core.utils.isNotNullEmpty
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class VmessFormatter @Inject constructor(
    val configManager: Provider<ConfigManager>,
    val storage: KeyValueStorage,
    val gson: Gson
) : BaseFormatter() {

    fun parse(str: String): ConfigProfileItem? {
        if (str.indexOf('?') > 0 && str.indexOf('&') > 0) {
            return parseVmessStd(str)
        }

        val allowInsecure = false
        val config = ConfigProfileItem.create(EConfigType.VMESS)

        var result = str.replace(EConfigType.VMESS.protocolScheme, "")
        result = Utils.decode(result)
        if (TextUtils.isEmpty(result)) {
            Log.w(Constants.TAG, "Toast decoding failed")
            return null
        }
        val vmessQRCode = gson.fromJson(result, VmessQRCode::class.java)
        // Although VmessQRCode fields are non null, looks like Gson may still create null fields
        if (TextUtils.isEmpty(vmessQRCode.add)
            || TextUtils.isEmpty(vmessQRCode.port)
            || TextUtils.isEmpty(vmessQRCode.id)
            || TextUtils.isEmpty(vmessQRCode.net)
        ) {
            Log.w(Constants.TAG, "Toast incorrect protocol")
            return null
        }

        config.remarks = vmessQRCode.ps
        config.server = vmessQRCode.add
        config.serverPort = vmessQRCode.port
        config.password = vmessQRCode.id
        config.method =
            if (TextUtils.isEmpty(vmessQRCode.scy)) Constants.DEFAULT_SECURITY else vmessQRCode.scy

        config.network = vmessQRCode.net ?: NetworkType.TCP.type
        config.headerType = vmessQRCode.type
        config.host = vmessQRCode.host
        config.path = vmessQRCode.path

        when (NetworkType.fromString(config.network)) {
            NetworkType.KCP -> {
                config.seed = vmessQRCode.path
            }

            NetworkType.GRPC -> {
                config.mode = vmessQRCode.type
                config.serviceName = vmessQRCode.path
                config.authority = vmessQRCode.host
            }

            else -> {}
        }

        config.security = vmessQRCode.tls
        config.insecure = allowInsecure
        config.sni = vmessQRCode.sni
        config.fingerPrint = vmessQRCode.fp
        config.alpn = vmessQRCode.alpn

        return config
    }

    fun toUri(config: ConfigProfileItem): String {
        val vmessQRCode = VmessQRCode()

        vmessQRCode.v = "2"
        vmessQRCode.ps = config.remarks
        vmessQRCode.add = config.server.orEmpty()
        vmessQRCode.port = config.serverPort.orEmpty()
        vmessQRCode.id = config.password.orEmpty()
        vmessQRCode.scy = config.method.orEmpty()
        vmessQRCode.aid = "0"

        vmessQRCode.net = config.network.orEmpty()
        vmessQRCode.type = config.headerType.orEmpty()
        when (NetworkType.fromString(config.network)) {
            NetworkType.KCP -> {
                vmessQRCode.path = config.seed.orEmpty()
            }

            NetworkType.GRPC -> {
                vmessQRCode.type = config.mode.orEmpty()
                vmessQRCode.path = config.serviceName.orEmpty()
                vmessQRCode.host = config.authority.orEmpty()
            }

            else -> {}
        }

        config.host.let { if (it.isNotNullEmpty()) vmessQRCode.host = it.orEmpty() }
        config.path.let { if (it.isNotNullEmpty()) vmessQRCode.path = it.orEmpty() }

        vmessQRCode.tls = config.security.orEmpty()
        vmessQRCode.sni = config.sni.orEmpty()
        vmessQRCode.fp = config.fingerPrint.orEmpty()
        vmessQRCode.alpn = config.alpn.orEmpty()

        val json = gson.toJson(vmessQRCode)
        return Utils.encode(json)
    }

    fun parseVmessStd(str: String): ConfigProfileItem? {
        val allowInsecure = false
        val config = ConfigProfileItem.create(EConfigType.VMESS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo
        config.method = Constants.DEFAULT_SECURITY

        getItemFormQuery(config, queryParam, allowInsecure)

        return config
    }

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configManager.get().createInitOutbound(EConfigType.VMESS)

        outboundBean?.settings?.vnext?.first()?.let { vnext ->
            vnext.address = profileItem.server.orEmpty()
            vnext.port = profileItem.serverPort.orEmpty().toInt()
            vnext.users[0].id = profileItem.password.orEmpty()
            vnext.users[0].security = profileItem.method
        }

        val sni = outboundBean?.streamSettings?.let {
            configManager.get().populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            configManager.get().populateTlsSettings(it, profileItem, sni)
        }

        return outboundBean
    }
}
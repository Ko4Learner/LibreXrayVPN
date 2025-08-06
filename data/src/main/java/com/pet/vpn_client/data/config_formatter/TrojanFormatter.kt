package com.pet.vpn_client.data.config_formatter

import com.pet.vpn_client.core.utils.Constants
import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.domain.models.NetworkType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class TrojanFormatter @Inject constructor(
    val configRepository: Provider<ConfigRepository>,
    val storage: KeyValueStorage
) : BaseFormatter() {
    fun parse(str: String): ConfigProfileItem? {
        val allowInsecure = false
        val config = ConfigProfileItem.create(EConfigType.TROJAN)

        val uri = URI(Utils.fixIllegalUrl(str))
        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo

        if (uri.rawQuery.isNullOrEmpty()) {
            config.network = NetworkType.TCP.type
            config.security = Constants.TLS
            config.insecure = allowInsecure
        } else {
            val queryParam = getQueryParam(uri)

            getItemFormQuery(config, queryParam, allowInsecure)
            config.security = queryParam[SECURITY] ?: Constants.TLS
        }

        return config
    }

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configRepository.get().createInitOutbound(EConfigType.TROJAN)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            server.password = profileItem.password
            server.flow = profileItem.flow
        }

        val sni = outboundBean?.streamSettings?.let {
            configRepository.get().populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            configRepository.get().populateTlsSettings(it, profileItem, sni)
        }

        return outboundBean
    }

    companion object{
        private const val SECURITY = "security"
    }
}
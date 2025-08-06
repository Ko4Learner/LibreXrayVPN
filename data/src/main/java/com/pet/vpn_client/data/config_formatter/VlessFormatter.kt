package com.pet.vpn_client.data.config_formatter

import com.pet.vpn_client.domain.interfaces.repository.ConfigRepository
import com.pet.vpn_client.domain.models.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class VlessFormatter @Inject constructor(
    val configRepository: Provider<ConfigRepository>,
    val storage: KeyValueStorage
) : BaseFormatter() {
    fun parse(str: String): ConfigProfileItem? {
        val allowInsecure = false
        val config = ConfigProfileItem.create(EConfigType.VLESS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo
        config.method = queryParam[ENCRYPTION] ?: NONE

        getItemFormQuery(config, queryParam, allowInsecure)

        return config
    }

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configRepository.get().createInitOutbound(EConfigType.VLESS)

        outboundBean?.settings?.vnext?.first()?.let { vnext ->
            vnext.address = profileItem.server.orEmpty()
            vnext.port = profileItem.serverPort.orEmpty().toInt()
            vnext.users[0].id = profileItem.password.orEmpty()
            vnext.users[0].encryption = profileItem.method
            vnext.users[0].flow = profileItem.flow
        }

        val sni = outboundBean?.streamSettings?.let {
            configRepository.get().populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            configRepository.get().populateTlsSettings(it, profileItem, sni)
        }

        return outboundBean
    }

    companion object {
        private const val ENCRYPTION = "encryption"
        private const val NONE = "none"
    }
}
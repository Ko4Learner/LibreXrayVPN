package com.pet.vpn_client.data.config_formatter

import com.pet.vpn_client.app.Constants
import com.pet.vpn_client.data.ConfigManager
import com.pet.vpn_client.data.dto.XrayConfig.OutboundBean
import com.pet.vpn_client.domain.interfaces.KeyValueStorage
import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.utils.Utils
import com.pet.vpn_client.utils.idnHost
import java.net.URI
import javax.inject.Inject

class VlessFormatter @Inject constructor(
    val configManager: ConfigManager,
    val storage: KeyValueStorage
) : BaseFormatter() {

    fun parse(str: String): ConfigProfileItem? {
        var allowInsecure = storage.decodeSettingsBool(Constants.PREF_ALLOW_INSECURE, false)
        val config = ConfigProfileItem.create(EConfigType.VLESS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo
        config.method = queryParam["encryption"] ?: "none"

        getItemFormQuery(config, queryParam, allowInsecure)

        return config
    }

    fun toUri(config: ConfigProfileItem): String {
        val dicQuery = getQueryDic(config)
        dicQuery["encryption"] = config.method ?: "none"

        return toUri(config, config.password, dicQuery)
    }

    fun toOutbound(profileItem: ConfigProfileItem): OutboundBean? {
        val outboundBean = configManager.createInitOutbound(EConfigType.VLESS)

        outboundBean?.settings?.vnext?.first()?.let { vnext ->
            vnext.address = profileItem.server.orEmpty()
            vnext.port = profileItem.serverPort.orEmpty().toInt()
            vnext.users[0].id = profileItem.password.orEmpty()
            vnext.users[0].encryption = profileItem.method
            vnext.users[0].flow = profileItem.flow
        }

        val sni = outboundBean?.streamSettings?.let {
            configManager.populateTransportSettings(it, profileItem)
        }

        outboundBean?.streamSettings?.let {
            configManager.populateTlsSettings(it, profileItem, sni)
        }

        return outboundBean
    }
}
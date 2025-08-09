package com.pet.vpn_client.data.protocol_parsers

import com.pet.vpn_client.domain.models.ConfigProfileItem
import com.pet.vpn_client.domain.models.EConfigType
import com.pet.vpn_client.core.utils.Utils
import com.pet.vpn_client.core.utils.idnHost
import java.net.URI
import javax.inject.Inject

class SocksParser @Inject constructor() : BaseParser() {
    fun parse(str: String): ConfigProfileItem? {
        val config = ConfigProfileItem.create(EConfigType.SOCKS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        if (uri.userInfo?.isEmpty() == false) {
            val result = Utils.decode(uri.userInfo).split(":", limit = 2)
            if (result.count() == 2) {
                config.username = result.first()
                config.password = result.last()
            }
        }

        return config
    }
}
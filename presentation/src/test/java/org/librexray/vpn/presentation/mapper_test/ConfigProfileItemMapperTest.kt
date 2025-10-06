package org.librexray.vpn.presentation.mapper_test

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.librexray.vpn.domain.models.ConfigProfileItem
import org.librexray.vpn.domain.models.ConfigType
import org.librexray.vpn.presentation.mapper.toServerItemModel
import org.librexray.vpn.presentation.model.ServerItemModel

class ConfigProfileItemMapperTest {
    @Test
    fun `maps ConfigProfileItem to ServerItemModel with masked IP`() {
        val profile = ConfigProfileItem(
            configType = ConfigType.VMESS,
            remarks = "My Server",
            server = "192.168.1.10",
            serverPort = "443"
        )

        val result = profile.toServerItemModel("test-guid")

        assertThat(result).isEqualTo(
            ServerItemModel(
                guid = "test-guid",
                name = "My Server",
                ip = "192.168.1.*** : 443",
                protocol = "VMESS"
            )
        )
    }
}
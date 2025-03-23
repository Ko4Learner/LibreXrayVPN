package com.pet.vpn_client.data

import com.pet.vpn_client.domain.repository.VpnRepository

class VpnRepositoryImpl(
    private val myVpnService: MyVpnService
): VpnRepository
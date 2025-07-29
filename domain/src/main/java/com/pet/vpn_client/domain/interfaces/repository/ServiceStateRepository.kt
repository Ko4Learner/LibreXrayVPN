package com.pet.vpn_client.domain.interfaces.repository

import com.pet.vpn_client.domain.state.ServiceState
import kotlinx.coroutines.flow.StateFlow

interface ServiceStateRepository {
    val serviceState: StateFlow<ServiceState>
    fun updateState(state: ServiceState)
}
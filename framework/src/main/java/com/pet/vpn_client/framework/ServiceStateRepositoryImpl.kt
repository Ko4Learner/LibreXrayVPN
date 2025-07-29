package com.pet.vpn_client.framework

import com.pet.vpn_client.domain.interfaces.repository.ServiceStateRepository
import com.pet.vpn_client.domain.state.ServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ServiceStateRepositoryImpl @Inject constructor() :
    ServiceStateRepository {
    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    override val serviceState: StateFlow<ServiceState> = _serviceState
    override fun updateState(state: ServiceState) {
        _serviceState.value = state
    }
}
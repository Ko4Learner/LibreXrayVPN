package org.librexray.vpn.framework

import org.librexray.vpn.domain.interfaces.repository.ServiceStateRepository
import org.librexray.vpn.domain.state.ServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Repository implementation for managing VPN service state.
 */
class ServiceStateRepositoryImpl @Inject constructor() :
    ServiceStateRepository {
    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    override val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()
    /**
     * Updates the current VPN service state.
     *
     * @param state New [ServiceState] value.
     */
    override fun updateState(state: ServiceState) {
        _serviceState.value = state
    }
}
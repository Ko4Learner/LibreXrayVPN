package org.librexray.vpn.domain.interfaces.repository

import org.librexray.vpn.domain.state.ServiceState
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for the VPN service state machine.
 *
 * Contracts:
 * - serviceState:
 *     Hot stream that always exposes the **current** state on subscription (e.g., a StateFlow).
 */
interface ServiceStateRepository {
    val serviceState: StateFlow<ServiceState>
    fun updateState(state: ServiceState)
}
package com.pet.vpn_client.domain.state

sealed class ServiceState {
    object Idle : ServiceState()
    object Connected : ServiceState()
    object Stopped : ServiceState()
}
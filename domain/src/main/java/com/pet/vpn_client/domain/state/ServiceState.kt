package com.pet.vpn_client.domain.state

/**
 * Minimal VPN service state machine used across the app.
 *
 * Semantics:
 * - [Idle]     — no active tunnel; ready to start.
 * - [Connected]— tunnel established and routing is active.
 * - [Stopped]  — service fully stopped; resources released.
 */
sealed class ServiceState {
    object Idle : ServiceState()
    object Connected : ServiceState()
    object Stopped : ServiceState()
}
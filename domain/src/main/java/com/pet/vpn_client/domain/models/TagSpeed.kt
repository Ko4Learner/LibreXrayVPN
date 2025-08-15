package com.pet.vpn_client.domain.models

data class TagSpeed(
    val tag: String,
    val uplinkBps: Double,
    val downlinkBps: Double,
    val timestampMs: Long
)
package com.pet.vpn_client.domain.models

data class FrameData(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val imageFormat: Int
)
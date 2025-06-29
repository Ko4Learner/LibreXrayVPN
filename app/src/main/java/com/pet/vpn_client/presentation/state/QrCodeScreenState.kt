package com.pet.vpn_client.presentation.state

data class QrCodeScreenState(
    val isLoading: Boolean = false,
    val isQrCodeDetected: Boolean = false,
    val result: String? = null,
    val error: String? = null
)

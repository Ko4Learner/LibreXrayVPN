package com.pet.vpn_client.presentation.intent

sealed interface QrCodeScreenIntent {
    object ResetState: QrCodeScreenIntent
}
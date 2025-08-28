package org.librexray.vpn.presentation.intent

sealed interface QrCodeScreenIntent {
    object ResetState: QrCodeScreenIntent
}
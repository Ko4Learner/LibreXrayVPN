package org.librexray.vpn.presentation.state

data class QrCodeScreenState(
    val configFound: Boolean = false,
    val error: Boolean = false
)

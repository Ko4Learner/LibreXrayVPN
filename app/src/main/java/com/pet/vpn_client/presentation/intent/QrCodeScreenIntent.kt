package com.pet.vpn_client.presentation.intent

import android.net.Uri

sealed interface QrCodeScreenIntent {
    object StartCameraScan : QrCodeScreenIntent
    data class ScanFromGallery(val uri: Uri) : QrCodeScreenIntent
    data class OnQrScanned(val result: String) : QrCodeScreenIntent
    data class OnScanFailed(val reason: String) : QrCodeScreenIntent
    object ClearResult : QrCodeScreenIntent
}
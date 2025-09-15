package org.librexray.vpn.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.librexray.vpn.presentation.intent.QrCodeScreenIntent
import org.librexray.vpn.presentation.view_model.QrCodeScreenViewModel
import org.librexray.vpn.presentation.composable_elements.QrCameraPreview
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import org.librexray.vpn.core.utils.Constants
import kotlinx.coroutines.delay
import org.librexray.vpn.presentation.composable_elements.ScanMask
import org.librexray.vpn.presentation.design_system.icon.AppIcons

@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: QrCodeScreenViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onResult: () -> Unit,
    getString: (Int) -> String
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permission ->
        hasCameraPermission = permission
    }

    LaunchedEffect(Unit) {
        val cameraPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (cameraPermissionGranted) {
            hasCameraPermission = true
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = AppIcons.arrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
            Text(
                text = "Сканирование Qr кода",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )

        }

        if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                QrCameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onFrame = viewModel::onAnalyzeFrame
                )
                ScanMask()
            }
        } else {
            Log.d(Constants.TAG, "permission denied")
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.configFound) {
        if (state.configFound) {
            Toast.makeText(context, "Конфигурация найдена!", Toast.LENGTH_SHORT).show()
            delay(2000)
            viewModel.onIntent(QrCodeScreenIntent.ResetState)
            onResult()
        }
    }
}
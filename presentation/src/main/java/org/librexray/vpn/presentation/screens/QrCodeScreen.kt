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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.librexray.vpn.presentation.intent.QrCodeScreenIntent
import org.librexray.vpn.presentation.view_model.QrCodeScreenViewModel
import org.librexray.vpn.presentation.composable_element.QrCameraPreview
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import org.librexray.vpn.coreandroid.utils.Constants
import kotlinx.coroutines.delay
import org.librexray.vpn.coreandroid.R
import org.librexray.vpn.presentation.composable_element.ScanMask
import org.librexray.vpn.presentation.design_system.icon.AppIcons
import org.librexray.vpn.presentation.design_system.icon.rememberPainter
import org.librexray.vpn.presentation.design_system.theme.LibreXrayVPNTheme

@Composable
fun QrCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: QrCodeScreenViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onResult: () -> Unit
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permission ->
        hasCameraPermission = permission
    }

    QrCodeScreenContent(
        modifier = modifier,
        onBackClick = onBackClick,
        hasCameraPermission = hasCameraPermission,
        onAnalyzeFrame = viewModel::onAnalyzeFrame
    )

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

    LaunchedEffect(state.error) {
        if (state.error) {
            Toast.makeText(
                context,
                context.getString(R.string.scanning_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(state.configFound) {
        if (state.configFound) {
            Toast.makeText(
                context,
                context.getString(R.string.configuration_found),
                Toast.LENGTH_SHORT
            ).show()
            delay(2000)
            viewModel.onIntent(QrCodeScreenIntent.ResetState)
            onResult()
        }
    }
}

@Composable
fun QrCodeScreenContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    hasCameraPermission: Boolean,
    onAnalyzeFrame: (ImageProxy) -> Unit
) {
    val isPreview = LocalInspectionMode.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = onBackClick
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = AppIcons.arrowBack.rememberPainter(),
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colors.onBackground
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.qr_code_scanning),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onBackground
                )
            }

        }

        if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.qr_code_scanning),
                            color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.h6
                        )
                    }
                } else {
                    QrCameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onFrame = onAnalyzeFrame
                    )
                    ScanMask()
                }
            }
        } else {
            Log.d(Constants.TAG, "permission denied")
        }
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewQrCodeScreen() {
    LibreXrayVPNTheme {
        QrCodeScreenContent(onBackClick = {}, hasCameraPermission = true, onAnalyzeFrame = {})
    }
}
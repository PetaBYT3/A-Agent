package com.a.agent.presentation.util

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

data class AppPermissionState(
    val isGranted: Boolean,
    val requestPermission: () -> Unit
)

@Composable
fun rememberAppPermissionState(
    permission: String
): AppPermissionState {
    val context = LocalContext.current

    var isGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted = it }
    )

    val appPermissionState = AppPermissionState(
        isGranted = isGranted,
        requestPermission = { permissionLauncher.launch(permission) }
    )

    return appPermissionState
}
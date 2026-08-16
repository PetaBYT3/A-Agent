package com.a.agent.presentation.util

import android.Manifest
import android.os.Build

private val currentBuildVersionSdkInt = Build.VERSION.SDK_INT
private val isAndroid13OrAbove = currentBuildVersionSdkInt >= Build.VERSION_CODES.TIRAMISU
private val isAndroid10OrAbove = currentBuildVersionSdkInt >= Build.VERSION_CODES.Q

enum class PermissionRequest {
    Notification,
    Storage;

    val permissions: Array<String>
        get() = when (this) {
            Notification -> {
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            }
            Storage -> {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
}
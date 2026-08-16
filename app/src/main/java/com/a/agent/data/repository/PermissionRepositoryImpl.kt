package com.a.agent.data.repository

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.a.agent.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PermissionRepositoryImpl(
    private val application: Application
): PermissionRepository {
    private val _isNotificationPermissionGranted = MutableStateFlow(false)
    override val isNotificationPermissionGranted: Flow<Boolean> = _isNotificationPermissionGranted.asStateFlow()

    private val _isStoragePermissionGranted = MutableStateFlow(false)
    override val isStoragePermissionGranted: Flow<Boolean> = _isStoragePermissionGranted.asStateFlow()

    private val _isMicrophonePermissionGranted = MutableStateFlow(false)
    override val isMicrophonePermissionGranted: Flow<Boolean> = _isMicrophonePermissionGranted.asStateFlow()

    override fun updatePermission() {
        val currentBuildVersionSdkInt = Build.VERSION.SDK_INT
        val isAndroid13OrAbove = currentBuildVersionSdkInt >= Build.VERSION_CODES.TIRAMISU
        val isAndroid10OrAbove = currentBuildVersionSdkInt >= Build.VERSION_CODES.Q

        val notificationPermission = if (isAndroid13OrAbove) {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        _isNotificationPermissionGranted.update { notificationPermission }

        val storagePermission = when {
            isAndroid13OrAbove -> {
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            }
            isAndroid10OrAbove -> {
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                val readGranted = ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                val writeGranted = ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                readGranted && writeGranted
            }
        }
        _isStoragePermissionGranted.update { storagePermission }

        val microphonePermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        _isMicrophonePermissionGranted.update { microphonePermission }
    }
}
package com.a.agent.domain.repository

import kotlinx.coroutines.flow.Flow

interface PermissionRepository {
    val isNotificationPermissionGranted: Flow<Boolean>
    val isStoragePermissionGranted: Flow<Boolean>
    val isMicrophonePermissionGranted: Flow<Boolean>

    fun updatePermission()
}
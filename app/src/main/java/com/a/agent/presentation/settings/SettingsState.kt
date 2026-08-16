package com.a.agent.presentation.settings

data class SettingsState(
    val isExportBackupBottomSheetVisible: Boolean = false,
    val exportBackupFile: String? = null,
    val exportBackupProgress: Float = 0f,
    val isExportBackupComplete: Boolean = false,

    val isImportBackupBottomSheetVisible: Boolean = false,
    val importBackupFile: String? = null,
    val importBackupProgress: Float = 0f,
    val isImportBackupComplete: Boolean = false
)

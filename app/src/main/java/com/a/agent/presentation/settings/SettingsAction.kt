package com.a.agent.presentation.settings

import io.github.vinceglb.filekit.PlatformFile

sealed interface SettingsAction {
    data class ExportBackupButton(val platformFile: PlatformFile): SettingsAction
    data object ExportBackupDoneButton: SettingsAction

    data class ImportBackupButton(val backupFile: PlatformFile): SettingsAction

    data class ShowSnackBar(val message: String): SettingsAction
}
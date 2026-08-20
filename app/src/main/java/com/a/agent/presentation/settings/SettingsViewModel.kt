package com.a.agent.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.BackupRepository
import com.a.agent.presentation.navigation.Effect
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val backupRepository: BackupRepository
): ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ExportBackupButton -> exportBackupButton(action.platformFile)
            SettingsAction.ExportBackupDoneButton -> {
                _state.update { it.copy(isExportBackupBottomSheetVisible = false, isExportBackupComplete = false) }
            }
            is SettingsAction.ImportBackupButton -> importBackupButton(action.backupFile)
            is SettingsAction.ShowSnackBar -> {
                viewModelScope.launch { _effect.send(Effect.ShowSnackBar(action.message)) }
            }
        }
    }

    private fun exportBackupButton(platformFile: PlatformFile) {
        backupRepository.exportBackup(platformFile).onStart {
            _state.update {
                it.copy(
                    isExportBackupBottomSheetVisible = true,
                    exportBackupFile = "Getting Files..."
                )
            }
        }.onEach { either ->
            either.onRight { processStatus ->
                when (processStatus) {
                    is ProcessStatus.OnProcess -> {
                        _state.update {
                            it.copy(
                                exportBackupFile = processStatus.process.first,
                                exportBackupProgress = processStatus.process.second
                            )
                        }
                    }
                    ProcessStatus.OnCompletion -> {
                        _state.update {
                            it.copy(
                                isExportBackupComplete = true,
                                exportBackupFile = null
                            )
                        }
                    }
                }
            }.onLeft { error ->
                _state.update {
                    it.copy(
                        isExportBackupBottomSheetVisible = false,
                        exportBackupFile = null
                    )
                }
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun importBackupButton(platformFile: PlatformFile) {
        backupRepository.importBackup(platformFile).onStart {
            _state.update {
                it.copy(
                    isImportBackupBottomSheetVisible = true,
                    importBackupFile = "Reading Files..."
                )
            }
        }.onEach { either ->
            either.onRight { processStatus ->
                when (processStatus) {
                    is ProcessStatus.OnProcess -> {
                        _state.update {
                            it.copy(
                                importBackupFile = processStatus.process.first,
                                importBackupProgress = processStatus.process.second
                            )
                        }
                    }
                    ProcessStatus.OnCompletion -> {
                        _state.update {
                            it.copy(
                                isImportBackupComplete = true,
                                importBackupFile = null
                            )
                        }
                    }
                }
            }.onLeft { error ->
                _state.update {
                    it.copy(
                        isImportBackupBottomSheetVisible = false,
                        importBackupFile = null
                    )
                }
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }
}
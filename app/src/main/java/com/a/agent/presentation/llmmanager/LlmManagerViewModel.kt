@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.presentation.llmmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.domain.repository.LlmRepository
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.BackStack
import com.a.agent.presentation.navigation.NavigationDisplayBackStack
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi

class LlmManagerViewModel(
    val modelId: String,
    private val llmRepository: LlmRepository,
    private val navigationDisplayBackStack: NavigationDisplayBackStack
): ViewModel() {
    private val _state = MutableStateFlow(LlmManagerState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LlmManagerState())

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private fun initialize() {
        if (modelId.isNotBlank()) {
            llmRepository.getLlm(modelId).onStart {
                _state.update { it.copy(isOnEdit = true) }
            }.onEach { either ->
                either.onRight { llmEntity ->
                    _state.update {
                        it.copy(
                            llm = llmEntity,
                            llmUrlTextField = llmEntity?.url ?: "",
                            llmSourceChip = llmEntity?.llmSource ?: LlmSource.Url,
                            llmNameTextField = llmEntity?.name ?: "",
                            llmFileName = llmEntity?.fileName ?: "",
                            llmFileSize = llmEntity?.totalBytes ?: 0,
                            isLlmSupported = true,
                            isLlmLoading = false
                        )
                    }
                }.onLeft { error ->
                    _state.update { it.copy(isLlmError = error, isLlmLoading = false) }
                }
            }.launchIn(viewModelScope)
        } else {
            _state.update { it.copy(isLlmLoading = false) }
        }
    }

    fun onAction(action: LlmManagerAction) {
        when (action) {
            is LlmManagerAction.LlmSourceChip -> llmSourceChip(action.llmSource)
            is LlmManagerAction.LocalFilePicker -> localFilePicker(action.platformFile)
            is LlmManagerAction.UrlTextField -> urlTextField(action.url)
            is LlmManagerAction.NameTextField -> {
                _state.update { it.copy(llmNameTextField = action.name) }
            }
            LlmManagerAction.DeleteLlmBottomSheet -> {
                _state.update { it.copy(isDeleteLlmBottomSheetVisible = !it.isDeleteLlmBottomSheetVisible) }
            }
            LlmManagerAction.DeleteLlmButton -> deleteLlmButton()
            LlmManagerAction.UpsertLlmButton -> upsertLlmButton()
        }
    }

    private fun llmSourceChip(llmSource: LlmSource) {
        if (_state.value.llmSourceChip != llmSource) {
            _state.update { it.copy(llmSourceChip = llmSource) }
            clearLlmMetadata()
        }
    }

    private fun localFilePicker(platformFile: PlatformFile) {
        llmRepository.getMetadataFromLocal(platformFile).onStart {
            clearLlmMetadata()
            _state.update { it.copy(isMetadataLoading = true) }
        }.onEach { either ->
            either.onRight { metadata ->
                _state.update {
                    it.copy(
                        llmLocalPlatformFile = platformFile,
                        llmFilePath = metadata.filePath,
                        llmFileName = metadata.fileName,
                        llmFileSize = metadata.totalBytes,
                        isLlmSupported = metadata.isSupported,
                        isMetadataLoading = false
                    )
                }
            }.onLeft { error ->
                _state.update { it.copy(isMetadataError = error, isMetadataLoading = false) }
            }
        }.launchIn(viewModelScope)
    }
    private var searchJob: Job? = null
    private fun urlTextField(url: String) {
        _state.update { it.copy(llmUrlTextField = url) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500.milliseconds)
            if (url.isBlank()) return@launch

            llmRepository.getMetadataFromUrl(url).onStart {
                _state.update {
                    it.copy(
                        llmLocalPlatformFile = null,
                        llmFileName = "",
                        llmFileSize = 0,
                        isLlmSupported = false,
                        isMetadataError = null,
                        isMetadataLoading = true
                    )
                }
            }.onEach { either ->
                either.onRight { metadata ->
                    _state.update {
                        it.copy(
                            llmFilePath = metadata.filePath,
                            llmFileName = metadata.fileName,
                            llmFileSize = metadata.totalBytes,
                            isLlmSupported = metadata.isSupported,
                            isMetadataLoading = false
                        )
                    }
                }.onLeft { error ->
                    _state.update { it.copy(isMetadataError = error, isMetadataLoading = false) }
                }
            }.collect()
        }
    }

    private fun deleteLlmButton() {
        val llm = _state.value.llm ?: return
        llmRepository.deleteLlm(llm).onEach { either ->
            either.onRight {
                navigationDisplayBackStack.sendEvent(BackStack.PopBackStack)
            }.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun upsertLlmButton() {
        val llmLocalPlatformFile = _state.value.llmLocalPlatformFile

        val llmState = _state.value.llm
        val llmEntity = llmState?.copy(
            name = _state.value.llmNameTextField
        ) ?: LlmEntity(
            name = _state.value.llmNameTextField,
            url = _state.value.llmUrlTextField,
            path = _state.value.llmFilePath,
            fileName = _state.value.llmFileName,
            totalBytes = _state.value.llmFileSize,
            llmSource = _state.value.llmSourceChip,
            isDownloaded = llmLocalPlatformFile != null
        )
        llmRepository.upsertLlm(llmEntity).onStart {
            _state.update { it.copy(isUpsertLlmButtonLoading = true) }
            if (llmLocalPlatformFile != null) llmRepository.copyFromLocal(llmLocalPlatformFile).collect()
        }.onEach { either ->
            either.onRight {
                navigationDisplayBackStack.sendEvent(BackStack.PopBackStack)
            }.onLeft { error ->
                _state.update { it.copy(isUpsertLlmButtonLoading = false) }
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun clearLlmMetadata() {
        _state.update {
            it.copy(
                llmUrlTextField = "",
                llmLocalPlatformFile = null,
                llmNameTextField = "",
                llmFileName = "",
                llmFileSize = 0,
                isLlmSupported = false
            )
        }
    }
}
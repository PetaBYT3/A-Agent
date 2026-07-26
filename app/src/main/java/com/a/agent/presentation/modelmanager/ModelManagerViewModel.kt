@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.presentation.modelmanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ModelManagerViewModel(
    val modelId: String,
    private val application: Application,
    private val llmModelManagerRepository: LlmModelManagerRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ModelManagerState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelManagerState())

    private fun initialize() {
        viewModelScope.launch {
            if (modelId.isNotBlank()) {
                llmModelManagerRepository.getModel(modelId).collect { either ->
                    either.onRight { modelEntity ->
                        _state.update {
                            it.copy(
                                isOnEdit = true,
                                model = modelEntity,
                                isModelSupported = true,
                                isModelLoading = false
                            )
                        }
                    }.onLeft { error ->
                        _state.update { it.copy(isModelError = error, isModelLoading = false) }
                    }
                }
            }
        }
    }

    fun onAction(action: ModelManagerAction) {
        when (action) {
            is ModelManagerAction.LlmSourceChip -> {
                _state.update { it.copy(model = it.model.copy(fileName = "", path = "", totalBytes = 0, modelSource = action.llmSource)) }
            }
            is ModelManagerAction.LocalFilePicker -> localFilePicker(action.platformFile)
            is ModelManagerAction.UrlTextField -> urlTextField(action.url)
            is ModelManagerAction.NameTextField -> {
                _state.update { it.copy(model = it.model.copy(name = action.name)) }
            }
            ModelManagerAction.UpsertModel -> upsertModel()
            ModelManagerAction.DeleteModel -> deleteModel()
        }
    }

    private fun localFilePicker(platformFile: PlatformFile) = viewModelScope.launch {
        _state.update { it.copy(isMetadataLoading = true) }

        val targetFile = File(application.getExternalFilesDir(null), "model" + File.separator + platformFile.name)
        withContext(Dispatchers.IO) {
            application.contentResolver.openInputStream(platformFile.toAndroidUri()).use { inputStream ->
                targetFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                    outputStream.flush()
                }
            }
        }

        _state.update {
            it.copy(
                isMetadataLoading = false,
                model = it.model.copy(
                    fileName = platformFile.name,
                    path = targetFile.absolutePath,
                    totalBytes = platformFile.size(),
                    isDownloaded = true
                ),
                isModelSupported = platformFile.name.endsWith(".litertlm", ignoreCase = true)
            )
        }
    }

    private var searchJob: Job? = null
    private fun urlTextField(url: String) {
        _state.update { it.copy(model = it.model.copy(url = url)) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500.milliseconds)
            if (url.isNotBlank()) {
                llmModelManagerRepository.getModelMetadata(url).onStart {
                    _state.update {
                        it.copy(
                            model = it.model.copy(
                                fileName = "",
                                totalBytes = 0
                            ),
                            isMetadataLoading = true,
                            isMetadataError = null
                        )
                    }
                }.collect { either ->
                    either.onRight { modelMetadataDto ->
                        val targetFile = File(application.getExternalFilesDir(null), "model" + File.separator + modelMetadataDto.fileName)
                        _state.update {
                            it.copy(
                                model = it.model.copy(
                                    fileName = modelMetadataDto.fileName,
                                    path = targetFile.absolutePath,
                                    totalBytes = modelMetadataDto.totalBytes,
                                ),
                                isModelSupported = modelMetadataDto.isSupported,
                                isMetadataLoading = false
                            )
                        }
                    }.onLeft { error ->
                        _state.update { it.copy(isMetadataError = error, isMetadataLoading = false) }
                    }
                }
            }
        }
    }

    private fun upsertModel() = viewModelScope.launch {
        val modelEntity = _state.value.model.copy(
            id = _state.value.model.id.ifBlank { Uuid.random().toString() }
        )
        llmModelManagerRepository.upsertModel(modelEntity).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun deleteModel() = viewModelScope.launch {
        llmModelManagerRepository.deleteModel(_state.value.model).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }
}
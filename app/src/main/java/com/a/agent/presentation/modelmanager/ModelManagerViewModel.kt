@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.presentation.modelmanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelSource
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
                llmModelManagerRepository.getModel(modelId).first().onRight { modelEntity ->
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

    fun onAction(action: ModelManagerAction) {
        when (action) {
            is ModelManagerAction.UrlTextField -> urlTextField(action.url)
            is ModelManagerAction.NameTextField -> {
                _state.update { it.copy(model = it.model.copy(name = action.name)) }
            }
            ModelManagerAction.UpsertModel -> upsertModel()
            ModelManagerAction.DeleteModel -> deleteModel()
        }
    }

    private var searchJob: Job? = null

    private fun urlTextField(url: String) {
        _state.update { it.copy(model = it.model.copy(url = url)) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500.milliseconds)
            if (url.isNotBlank()) {
                llmModelManagerRepository.getModelMetadata(
                    url = _state.value.model.url
                ).onStart {
                    _state.update { it.copy(model = ModelEntity.Empty, isMetadataLoading = true, isMetadataError = null) }
                }.collect { either ->
                    either.onRight { modelMetadataDto ->
                        _state.update {
                            it.copy(
                                model = it.model.copy(
                                    fileName = modelMetadataDto.fileName,
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
            id = _state.value.model.id.ifBlank { Uuid.random().toString() },
            path = _state.value.model.path.ifBlank {
                File(application.getExternalFilesDir(null), "model" + File.separator + _state.value.model.fileName).absolutePath
            },
            modelSource = ModelSource.Url,
            isDownloaded = false
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
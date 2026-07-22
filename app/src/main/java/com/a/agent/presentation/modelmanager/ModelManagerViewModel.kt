@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.presentation.modelmanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.domain.usecase.ModelUseCases
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
                llmModelManagerRepository.getModel(modelId).collect { either ->
                    either.onRight { modelEntity ->
                        _state.update {
                            it.copy(
                                isOnEdit = true,
                                isMetadataLoading = true,
                                modelEntityToEdit = modelEntity,
                                nameTextField = modelEntity.name,
                                fileName = modelEntity.fileName,
                                totalBytes = modelEntity.totalBytes,
                                urlTextField = modelEntity.url,
                                isSupported = true
                            )
                        }
                    }.onLeft { error ->
                        _state.update { it.copy(isMetadataError = error) }
                    }
                    _state.update { it.copy(isMetadataLoading = false) }
                }
            }
        }
    }

    fun onAction(action: ModelManagerAction) {
        when (action) {
            is ModelManagerAction.UrlTextField -> urlTextField(action.url)
            is ModelManagerAction.NameTextField -> {
                _state.update { it.copy(nameTextField = action.name) }
            }
            ModelManagerAction.UpsertModel -> upsertModel()
            ModelManagerAction.DeleteModel -> deleteModel()
        }
    }

    private var searchJob: Job? = null

    private fun urlTextField(url: String) {
        _state.update { it.copy(urlTextField = url) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500.milliseconds)
            if (url.isNotBlank()) {
                llmModelManagerRepository.getModelMetadata(
                    url = _state.value.urlTextField
                ).onStart {
                    _state.update { it.copy(isMetadataLoading = true, isMetadataError = null) }
                }.collect { either ->
                    either.onRight { modelMetadataDto ->
                        _state.update {
                            it.copy(
                                fileName = modelMetadataDto.fileName,
                                totalBytes = modelMetadataDto.totalBytes,
                                isSupported = modelMetadataDto.isSupported
                            )
                        }
                    }.onLeft { error ->
                        _state.update { it.copy(isMetadataError = error) }
                    }
                    _state.update { it.copy(isMetadataLoading = false) }
                }
            }
        }
    }

    private fun upsertModel() = viewModelScope.launch {
        val modelEntity = ModelEntity(
            id = modelId.ifBlank { Uuid.random().toString() },
            name = _state.value.nameTextField,
            url = _state.value.urlTextField,
            path = File(application.getExternalFilesDir(null), "model" + File.separator + _state.value.fileName).absolutePath,
            fileName = _state.value.fileName,
            totalBytes = _state.value.totalBytes,
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
        llmModelManagerRepository.deleteModel(_state.value.modelEntityToEdit).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }
}
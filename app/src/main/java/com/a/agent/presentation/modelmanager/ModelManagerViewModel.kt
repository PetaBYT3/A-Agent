@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.presentation.modelmanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.usecase.ModelUseCases
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ModelManagerViewModel(
    val modelId: String,
    private val modelUseCases: ModelUseCases,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ModelManagerState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelManagerState())

    private fun initialize() = viewModelScope.launch {
        coroutineScope {
            launch {
                if (modelId.isNotBlank()) {
                    modelUseCases.getModel(
                        modelId = modelId
                    ).collect { either ->
                        either.onRight { modelEntity ->
                            _state.update {
                                it.copy(
                                    isOnEdit = true,
                                    urlTextField = modelEntity.url,
                                    nameTextField = modelEntity.name,
                                    typeRadioButton = modelEntity.type,
                                    fileName = modelEntity.fileName,
                                    totalBytes = modelEntity.totalBytes,
                                    isSupported = modelEntity.isSupported,
                                    isMetadataError = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private var searchJob: Job? = null

    fun onAction(action: ModelManagerAction) {
        when (action) {
            is ModelManagerAction.UrlTextField -> modelUrlTextField(action.url)
            is ModelManagerAction.NameTextField -> {
                _state.update { it.copy(nameTextField = action.name) }
            }
            is ModelManagerAction.TypeRadioButton -> {
                _state.update { it.copy(typeRadioButton = action.type) }
            }
            ModelManagerAction.UpsertModel -> upsertModel()
            ModelManagerAction.DeleteModel -> deleteModel()
        }
    }

    private fun modelUrlTextField(url: String) {
        _state.update { it.copy(urlTextField = url) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500.milliseconds)
            if (url.isNotBlank()) {
                _state.update { it.copy(isMetadataLoading = true) }
                modelUseCases.getModelMetadata(
                    url = _state.value.urlTextField
                ).onRight { modelMetadataDto ->
                    _state.update {
                        it.copy(
                            fileName = modelMetadataDto.fileName,
                            totalBytes = modelMetadataDto.totalBytes,
                            isSupported = modelMetadataDto.isSupported,
                            isMetadataError = null
                        )
                    }
                }.onLeft { e ->
                    _state.update { it.copy(isMetadataError = e) }
                }
                _state.update { it.copy(isMetadataLoading = false) }
            }
        }
    }

    private fun upsertModel() = viewModelScope.launch {
        modelUseCases.upsertModel(
            id = modelId.ifBlank { Uuid.random().toString() },
            url = _state.value.urlTextField,
            name = _state.value.nameTextField,
            type = _state.value.typeRadioButton,
            filename = _state.value.fileName,
            totalBytes = _state.value.totalBytes,
            isSupported = _state.value.isSupported
        ).onRight {
            navigationDisplayEvent.sendEvent(Event.PopBackStack)
        }.onLeft { e ->
            navigationDisplayEvent.sendEvent(Event.ShowSnackBar(e))
        }
    }

    private fun deleteModel() = viewModelScope.launch {
        modelUseCases.getModel(modelId).first().onRight { modelEntity ->
            modelUseCases.deleteModel(modelEntity).onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { e ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(e))
            }
        }
    }
}
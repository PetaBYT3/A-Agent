package com.a.agent.presentation.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelSource
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModelViewModel(
    private val llmModelManagerRepository: LlmModelManagerRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ModelState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelState())

    private fun initialize() {
        viewModelScope.launch {
            llmModelManagerRepository.activeDownloadInfo.collect { map ->
                _state.update { it.copy(downloadState = map) }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getModels().onStart {
                _state.update { it.copy(isAllModelsLoading = true) }
            }.collect { either ->
                either.onRight { models ->
                    _state.update { it.copy(allModels = models, isAllModelsLoading = false) }
                }.onLeft { error ->
                    _state.update { it.copy(isAllModelsError = error, isAllModelsLoading = false) }
                }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getModels(ModelSource.Default).onStart {
                _state.update { it.copy(isDefaultModelsLoading = true) }
            }.collect { either ->
                either.onRight { models ->
                    _state.update { it.copy(defaultModels = models, isDefaultModelsLoading = false) }
                }.onLeft { error ->
                    _state.update { it.copy(isDefaultModelsError = error, isDefaultModelsLoading = false) }
                }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getModels(ModelSource.Url).onStart {
                _state.update { it.copy(isUrlModelsLoading = true) }
            }.collect { either ->
                either.onRight { models ->
                    _state.update { it.copy(urlModels = models, isUrlModelsLoading = false) }
                }.onLeft { error ->
                    _state.update { it.copy(isUrlModelsError = error, isUrlModelsLoading = false) }
                }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getModels(ModelSource.Local).onStart {
                _state.update { it.copy(isLocalModelsLoading = true) }
            }.collect { either ->
                either.onRight { models ->
                    _state.update { it.copy(localModels = models, isLocalModelsLoading = false) }
                }.onLeft { error ->
                    _state.update { it.copy(isLocalModelsError = error, isLocalModelsLoading = false) }
                }
            }
        }
    }

    fun onAction(action: ModelAction) {
        when (action) {
            is ModelAction.ToggleDownload -> toggleDownload(action.modelEntity)
        }
    }

    private fun toggleDownload(modelEntity: ModelEntity) = viewModelScope.launch {
        llmModelManagerRepository.toggleDownload(modelEntity).collect { either ->
            either.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }
}
package com.a.agent.presentation.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.LlmModelFilter
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.domain.usecase.ModelUseCases
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.coroutineScope
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
            llmModelManagerRepository.getModels(LlmModelFilter.Downloaded).collect { either ->
                either.onRight { modelEntities ->
                    _state.update { it.copy(downloadedModelEntities = modelEntities) }
                }.onLeft { error ->
                    _state.update { it.copy(downloadedModelError = error) }
                }
            }
        }

        viewModelScope.launch {
            llmModelManagerRepository.getModels(LlmModelFilter.RequestDownload).collect { either ->
                either.onRight { modelEntities ->
                    _state.update { it.copy(requireDownloadModelEntities = modelEntities) }
                }.onLeft { error ->
                    _state.update { it.copy(requireDownloadModelError = error) }
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
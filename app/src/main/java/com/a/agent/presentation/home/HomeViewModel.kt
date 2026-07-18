package com.a.agent.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ConversationEntity
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.LlmModelFilter
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.domain.repository.LlmModelRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    private val llmModelManagerRepository: LlmModelManagerRepository,
    private val llmModelRepository: LlmModelRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    private fun initialize() {
        viewModelScope.launch {
            llmModelManagerRepository.getSelectedModel().collect { either ->
                either.onRight { modelEntity ->
                    _state.update { it.copy(selectedModelEntity = modelEntity) }
                }.onLeft { error ->
                    _state.update { it.copy(selectedModelError = error) }
                }
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
            llmModelRepository.isLlmModelEngineOnline.collect { isModelEngineOnline ->
                _state.update { it.copy(isModelEngineOnline = isModelEngineOnline) }
            }
        }

        viewModelScope.launch {
            llmModelRepository.getConversations().collect { either ->
                either.onRight { conversationEntities ->
                    _state.update { it.copy(conversationEntities = conversationEntities) }
                }.onLeft { error ->
                    _state.update { it.copy(conversationError = error) }
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.ToggleModelEngine -> toggleModelEngine()
            HomeAction.DownloadedModelBottomSheet -> {
                _state.update { it.copy(downloadedModelBottomSheet = !it.downloadedModelBottomSheet) }
            }
            is HomeAction.SelectModel -> selectModel(action.modelEntity)
            HomeAction.UpsertConversationBottomSheet -> {
                _state.update { it.copy(upsertConversationBottomSheet = !it.upsertConversationBottomSheet) }
            }
            is HomeAction.ConversationNameTextField -> {
                _state.update { it.copy(conversationNameTextField = action.name) }
            }
            HomeAction.UpsertConversationButton -> upsertConversationButton()
        }
    }

    private fun toggleModelEngine() = viewModelScope.launch {
        _state.update { it.copy(isModelEngineLoading = true) }
        when (_state.value.isModelEngineOnline) {
            true -> {
                llmModelRepository.uninitializeEngine().collect { either ->
                    either.onRight {

                    }
                    _state.update { it.copy(isModelEngineLoading = false) }
                }
            }
            false -> {
                llmModelRepository.initializeEngine(File(_state.value.selectedModelEntity.path)).collect { either ->
                    either.onRight {

                    }
                    _state.update { it.copy(isModelEngineLoading = false) }
                }
            }
        }
    }

    private fun selectModel(modelEntity: ModelEntity) = viewModelScope.launch {
        llmModelManagerRepository.setSelectedModel(modelEntity.id).collect { either ->
            either.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun upsertConversationButton() = viewModelScope.launch {
        val conversationEntity = ConversationEntity(
            title = _state.value.conversationNameTextField
        )
        llmModelRepository.upsertConversation(conversationEntity).collect { either ->
            either.onRight {

            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
            _state.update { it.copy(conversationNameTextField = "") }
        }
    }
}
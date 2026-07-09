@file:OptIn(ExperimentalCoroutinesApi::class)

package com.a.agent.presentation.texttotext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.repository.LiteRepository
import com.a.agent.domain.usecase.ModelUseCases
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TextToTextViewModel(
    val modelId: String,
    private val modelUseCases: ModelUseCases,
    private val liteRepository: LiteRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(TextToTextState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TextToTextState())

    private fun initialize() {
        viewModelScope.launch {
            modelUseCases.getModel(modelId).collect { either ->
                either.onRight { modelEntity ->
                    _state.update { it.copy(modelEntity = modelEntity) }
                }
            }
        }

        viewModelScope.launch {
            _state.mapNotNull { it.modelEntity }.distinctUntilChanged().flatMapLatest { modelEntity ->
                liteRepository.initializeEngine(
                    modelId = modelEntity.id,
                    modelPath = modelEntity.path
                )
            }.collect { either ->
                either.onRight { chatEntities ->
                    _state.update { it.copy(chatList = chatEntities, isModelInitializing = false) }
                }.onLeft { e ->
                    _state.update { it.copy(isError = e, isModelInitializing = false) }
                }
            }
        }
    }

    fun onAction(action: TextToTextAction) {
        when (action) {
            TextToTextAction.ClearChat -> clearChat()
            is TextToTextAction.PromptTextField -> {
                _state.update { it.copy(promptTextField = action.prompt) }
            }
            TextToTextAction.GenerateButton -> generateButton()
        }
    }

    private fun clearChat() = viewModelScope.launch {
    }

    private fun generateButton() = viewModelScope.launch {
        if (_state.value.isModelThinking) {
            navigationDisplayEvent.sendEvent(Event.ShowSnackBar("Model Still Thinking"))
            return@launch
        }

        _state.update { it.copy(isModelThinking = true) }
        liteRepository.generateResponse(
            modelId = _state.value.modelEntity?.id!!,
            prompt = _state.value.promptTextField
        ).collect { either ->
            either.onLeft { e ->
                _state.update { it.copy(isModelThinking = false, promptTextField = "") }
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(e))
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            liteRepository.uninitializeEngine().first()
        }
    }
}
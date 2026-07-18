@file:OptIn(ExperimentalCoroutinesApi::class)

package com.a.agent.presentation.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.model.GenerateState
import com.a.agent.domain.repository.LlmModelRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationViewModel(
    val conversationId: String,
    private val llmModelRepository: LlmModelRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ConversationState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationState())

    private fun initialize() {
        viewModelScope.launch {
            llmModelRepository.initializeConversation(conversationId).collect { either ->
                either.onRight { initializeConversationResult ->
                    _state.update {
                        it.copy(
                            conversationEntity = initializeConversationResult.conversation,
                            chatEntities = initializeConversationResult.chatList,
                            isConversationInitializing = false
                        )
                    }
                }.onLeft { error ->
                    _state.update { it.copy(isConversationInitializeError = error, isConversationInitializing = false) }
                }
            }
        }
    }

    fun onAction(action: ConversationAction) {
        when (action) {
            ConversationAction.ClearChat -> clearChat()
            is ConversationAction.PromptTextField -> {
                _state.update { it.copy(promptTextField = action.prompt) }
            }
            ConversationAction.GenerateButton -> generateButton()
        }
    }

    private fun clearChat() = viewModelScope.launch {

    }

    private fun generateButton() = viewModelScope.launch {
        llmModelRepository.generateResponse(
            conversationId = conversationId,
            prompt = _state.value.promptTextField
        ).onStart {
            _state.update { it.copy(isModelThinking = true) }
        }.collect { either ->
            either.onRight { generateState ->
                when (generateState) {
                    GenerateState.Requested -> _state.update { it.copy(promptTextField = "") }
                    GenerateState.Generating -> _state.update { it.copy(isModelThinking = true) }
                    GenerateState.Generated -> _state.update { it.copy(isModelThinking = false) }
                }
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }
}
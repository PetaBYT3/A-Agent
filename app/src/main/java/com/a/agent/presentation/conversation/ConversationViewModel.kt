@file:OptIn(ExperimentalCoroutinesApi::class)

package com.a.agent.presentation.conversation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.model.GenerateState
import com.a.agent.domain.repository.LlmModelEngineRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import com.a.agent.presentation.util.saveToCacheDir
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationViewModel(
    val conversationId: String,
    private val application: Application,
    private val llmModelEngineRepository: LlmModelEngineRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ConversationState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationState())

    private fun initialize() {
        viewModelScope.launch {
            llmModelEngineRepository.isLlmModelEngineOnline.collect { bool ->
                _state.update { it.copy(isEngineOnline = bool) }
            }
        }

        viewModelScope.launch {
            llmModelEngineRepository.initializeConversation(conversationId).onStart {
                _state.update { it.copy(isConversationInitializing = true) }
            }.collect { either ->
                either.onRight { pair ->
                    _state.update {
                        it.copy(
                            conversationEntity = pair.first,
                            chatEntities = pair.second,
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
            is ConversationAction.ImageInputPicker -> imageInputPicker(action.image)
            is ConversationAction.PromptTextField -> {
                _state.update { it.copy(promptTextField = action.prompt) }
            }
            ConversationAction.ImageDialog -> {
                _state.update { it.copy(isImageDialogVisible = !it.isImageDialogVisible) }
            }
            ConversationAction.GenerateButton -> generateButton()
        }
    }

    private fun clearChat() = viewModelScope.launch {

    }

    private fun imageInputPicker(image: PlatformFile?) = viewModelScope.launch {
        val cachedImagePath = image?.saveToCacheDir(application, "jpg")
        _state.update { it.copy(imageInput = cachedImagePath) }
    }

    private fun generateButton() = viewModelScope.launch {
        llmModelEngineRepository.generateResponse(
            conversationId = conversationId,
            imageInput = _state.value.imageInput,
            prompt = _state.value.promptTextField
        ).onStart {
            _state.update { it.copy(isModelThinking = true) }
        }.collect { either ->
            either.onRight { generateState ->
                when (generateState) {
                    GenerateState.Requested -> _state.update { it.copy(imageInput = null, promptTextField = "") }
                    GenerateState.Generating -> _state.update { it.copy(isModelThinking = true) }
                    GenerateState.Generated -> _state.update { it.copy(isModelThinking = false) }
                }
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
                _state.update { it.copy(isModelThinking = false) }
            }
        }
    }

    override fun onCleared() {
        CoroutineScope(NonCancellable).launch {
            llmModelEngineRepository.uninitializeConversation().collect()
        }
    }
}
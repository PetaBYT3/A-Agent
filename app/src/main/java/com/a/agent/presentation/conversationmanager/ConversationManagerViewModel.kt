package com.a.agent.presentation.conversationmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.domain.repository.ConversationRepository
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class ConversationManagerViewModel(
    val conversationId: String,
    private val conversationRepository: ConversationRepository,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ConversationManagerState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationManagerState())

    private fun initialize() {
        viewModelScope.launch {
            if (conversationId.isNotBlank()) {
                conversationRepository.getConversation(conversationId).collect { either ->
                    either.onRight { conversationEntity ->
                        _state.update { it.copy(isOnEdit = true, conversation = conversationEntity, isConversationLoading = false) }
                    }.onLeft { error ->
                        _state.update { it.copy(isOnEdit = true, isConversationError = error, isConversationLoading = false) }
                    }
                }
            } else {
                _state.update { it.copy(isOnEdit = false, isConversationLoading = false) }
            }
        }
    }

    fun onAction(action: ConversationManagerAction) {
        when (action) {
            is ConversationManagerAction.ConversationTitleTextField -> {
                _state.update { it.copy(conversation = it.conversation.copy(title = action.title)) }
            }
            ConversationManagerAction.UpsertConversationButton -> upsertConversationButton()
            ConversationManagerAction.DeleteConversationButton -> deleteConversationButton()
        }
    }

    private fun upsertConversationButton() = viewModelScope.launch {
        val conversationEntity = _state.value.conversation.copy(
            id = _state.value.conversation.id.ifBlank { Uuid.random().toString() }
        )
        conversationRepository.upsertConversation(conversationEntity).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }

    private fun deleteConversationButton() = viewModelScope.launch {
        conversationRepository.deleteConversation(_state.value.conversation).collect { either ->
            either.onRight {
                navigationDisplayEvent.sendEvent(Event.PopBackStack)
            }.onLeft { error ->
                navigationDisplayEvent.sendEvent(Event.ShowSnackBar(error))
            }
        }
    }
}
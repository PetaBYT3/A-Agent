package com.a.agent.presentation.conversationmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.conversation.ConversationEntity
import com.a.agent.domain.repository.ConversationRepository
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.BackStack
import com.a.agent.presentation.navigation.NavigationDisplayBackStack
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ConversationManagerViewModel(
    val conversationId: String,
    private val conversationRepository: ConversationRepository,
    private val navigationDisplayBackStack: NavigationDisplayBackStack
): ViewModel() {
    private val _state = MutableStateFlow(ConversationManagerState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConversationManagerState())

    private val _effect = Channel<Effect>(Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private fun initialize() {
        if (conversationId.isNotBlank()) {
            conversationRepository.getConversation(conversationId).onStart {
                _state.update { it.copy(isOnEdit = true) }
            }.onEach { either ->
                either.onRight { conversationEntity ->
                    _state.update {
                        it.copy(
                            conversation = conversationEntity,
                            conversationTitleTextField = conversationEntity?.title ?: "",
                            isConversationLoading = false
                        )
                    }
                }.onLeft { error ->
                    _state.update { it.copy(isConversationError = error, isConversationLoading = false) }
                }
            }.launchIn(viewModelScope)
        } else {
            _state.update { it.copy(isConversationLoading = false) }
        }
    }

    fun onAction(action: ConversationManagerAction) {
        when (action) {
            is ConversationManagerAction.ConversationTitleTextField -> {
                _state.update { it.copy(conversationTitleTextField = action.title) }
            }
            ConversationManagerAction.UpsertConversationButton -> upsertConversationButton()
            ConversationManagerAction.DeleteConversationBottomSheet -> {
                _state.update { it.copy(isDeleteConversationBottomSheetVisible = !it.isDeleteConversationBottomSheetVisible) }
            }
            ConversationManagerAction.DeleteConversationButton -> deleteConversationButton()
        }
    }

    private fun upsertConversationButton() {
        val conversationEntity = _state.value.conversation?.copy(
            title = _state.value.conversationTitleTextField
        ) ?: ConversationEntity(
            title = _state.value.conversationTitleTextField
        )
        conversationRepository.upsertConversation(conversationEntity).onEach { either ->
            either.onRight {
                navigationDisplayBackStack.sendEvent(BackStack.PopBackStack)
            }.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }

    private fun deleteConversationButton() {
        val conversation = _state.value.conversation ?: return
        conversationRepository.deleteConversation(conversation).onEach { either ->
            either.onRight {
                navigationDisplayBackStack.sendEvent(BackStack.PopBackStack)
            }.onLeft { error ->
                _effect.send(Effect.ShowSnackBar(error))
            }
        }.launchIn(viewModelScope)
    }
}
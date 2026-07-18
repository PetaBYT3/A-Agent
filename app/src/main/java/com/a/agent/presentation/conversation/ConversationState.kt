package com.a.agent.presentation.conversation

import com.a.agent.data.local.ChatEntity
import com.a.agent.data.local.ConversationEntity

data class ConversationState(
    val isConversationInitializing: Boolean = true,
    val isConversationInitializeError: String? = null,

    val conversationEntity: ConversationEntity = ConversationEntity.Empty,
    val chatEntities: List<ChatEntity> = emptyList(),
    val isModelThinking: Boolean = false,
    val promptTextField: String = ""
)

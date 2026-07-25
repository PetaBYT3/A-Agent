package com.a.agent.presentation.conversation

import com.a.agent.data.local.ChatEntity
import com.a.agent.data.local.ConversationEntity

data class ConversationState(
    val isEngineOnline: Boolean? = null,

    val isConversationInitializing: Boolean = false,
    val isConversationInitializeError: String? = null,

    val conversationEntity: ConversationEntity = ConversationEntity.Empty,
    val chatEntities: List<ChatEntity> = emptyList(),
    val isModelThinking: Boolean = false,

    val imageInput: String? = null,
    val promptTextField: String = "",

    val isImageDialogVisible: Boolean = false,
    val imageView: Any? = null
)

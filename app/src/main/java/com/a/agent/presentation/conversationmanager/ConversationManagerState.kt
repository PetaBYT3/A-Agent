package com.a.agent.presentation.conversationmanager

import com.a.agent.data.local.ConversationEntity

data class ConversationManagerState(
    val isOnEdit: Boolean = false,

    val isConversationLoading: Boolean = true,
    val isConversationError: String? = null,
    val conversation: ConversationEntity = ConversationEntity.Empty,
)

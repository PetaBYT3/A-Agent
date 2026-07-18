package com.a.agent.domain.model

import com.a.agent.data.local.ChatEntity
import com.a.agent.data.local.ConversationEntity

data class InitializeConversationResult(
    val conversation: ConversationEntity,
    val chatList: List<ChatEntity>
)
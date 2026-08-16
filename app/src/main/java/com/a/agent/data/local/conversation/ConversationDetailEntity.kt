package com.a.agent.data.local.conversation

import androidx.room.Embedded

data class ConversationDetailEntity(
    @Embedded val conversation: ConversationEntity,
    val totalChats: Int
)

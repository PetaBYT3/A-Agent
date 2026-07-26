package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.ConversationEntity
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversation(id: String): Flow<Either<String, ConversationEntity>>
    fun upsertConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>>
    fun deleteConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>>
}
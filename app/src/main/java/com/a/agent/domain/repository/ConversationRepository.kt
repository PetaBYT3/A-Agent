package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.chat.ChatEntity
import com.a.agent.data.local.conversation.ConversationDetailEntity
import com.a.agent.data.local.conversation.ConversationEntity
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversation(id: String): Flow<Either<String, ConversationEntity?>>
    fun getConversations(): Flow<Either<String, List<ConversationEntity>>>
    fun getConversationDetails(): Flow<Either<String, List<ConversationDetailEntity>>>
    fun upsertConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>>
    fun deleteConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>>
    fun getChats(conversationId: String): Flow<Either<String, List<ChatEntity>>>
    fun clearChats(conversationId: String): Flow<Either<String, Unit>>
}
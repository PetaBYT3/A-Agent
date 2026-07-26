package com.a.agent.data.repository

import androidx.room.withTransaction
import arrow.core.Either
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.ConversationEntity
import com.a.agent.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ConversationRepositoryImpl(
    private val agentDatabase: AgentDatabase
): ConversationRepository {
    override fun getConversation(id: String): Flow<Either<String, ConversationEntity>> = flow {
        try {
            agentDatabase.conversationDao.getConversation(id).collect { conversationEntity ->
                emit(Either.Right(conversationEntity ?: ConversationEntity.Empty))
            }
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun upsertConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>> = flow {
        try {
            agentDatabase.conversationDao.upsertConversation(conversationEntity)
            emit(Either.Right("Conversation Upserted"))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>> = flow {
        try {
            agentDatabase.withTransaction {
                agentDatabase.conversationDao.deleteConversation(conversationEntity.id)
                agentDatabase.chatDao.clearChat(conversationEntity.id)
            }
            emit(Either.Right("Conversation Deleted"))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}
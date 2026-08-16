package com.a.agent.data.repository

import androidx.room.withTransaction
import arrow.core.Either
import com.a.agent.data.local.Database
import com.a.agent.data.local.chat.ChatEntity
import com.a.agent.data.local.conversation.ConversationDetailEntity
import com.a.agent.data.local.conversation.ConversationEntity
import com.a.agent.data.mapper.toMessage
import com.a.agent.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ConversationRepositoryImpl(
    private val database: Database
): ConversationRepository {
    override fun getConversation(id: String): Flow<Either<String, ConversationEntity?>> {
        return flow<Either<String, ConversationEntity?>> {
            database.conversationDao.getConversation(id).collect { conversationEntity ->
                emit(Either.Right(conversationEntity))
            }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getConversations(): Flow<Either<String, List<ConversationEntity>>> {
        return flow<Either<String, List<ConversationEntity>>> {
            database.conversationDao.getConversations().collect { conversationEntities ->
                emit(Either.Right(conversationEntities))
            }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getConversationDetails(): Flow<Either<String, List<ConversationDetailEntity>>> {
        return flow<Either<String, List<ConversationDetailEntity>>> {
            database.conversationDao.getConversationDetails().collect { conversationDetailEntities ->
                emit(Either.Right(conversationDetailEntities))
            }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun upsertConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>> {
        return flow<Either<String, String>> {
            database.conversationDao.upsertConversation(conversationEntity)
            emit(Either.Right("Conversation Upserted"))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun deleteConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>> {
        return flow<Either<String, String>> {
            database.withTransaction {
                database.conversationDao.deleteConversation(conversationEntity.id)
                database.conversationDao.clearChat(conversationEntity.id)
            }
            emit(Either.Right("Conversation Deleted"))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getChats(conversationId: String): Flow<Either<String, List<ChatEntity>>> {
        return flow<Either<String, List<ChatEntity>>> {
            database.chatDao.getChats(conversationId).collect { chatEntities ->
                emit(Either.Right(chatEntities))
            }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun clearChats(conversationId: String): Flow<Either<String, Unit>> {
        return flow<Either<String, Unit>> {
            database.conversationDao.clearChat(conversationId)
            emit(Either.Right(Unit))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }
}
package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.ConversationEntity
import com.a.agent.domain.model.GenerateState
import com.a.agent.domain.model.InitializeConversationResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface LlmModelEngineRepository {
    val isLlmModelEngineOnline: Flow<Boolean>

    suspend fun initializeEngine(modelPath: File): Flow<Either<String, Unit>>
    suspend fun uninitializeEngine(): Flow<Either<String, Unit>>

    suspend fun getConversations(): Flow<Either<String, List<ConversationEntity>>>
    suspend fun upsertConversation(conversationEntity: ConversationEntity): Flow<Either<String, Unit>>
    suspend fun initializeConversation(conversationId: String): Flow<Either<String, InitializeConversationResult>>
    suspend fun uninitializeConversation(): Flow<Either<String, Unit>>
    suspend fun generateResponse(conversationId: String, prompt: String): Flow<Either<String, GenerateState>>
}
package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.ChatEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TextToTextRepository {
    suspend fun initializeLlmInference(modelId: String, modelPath: File): Flow<Either<String, List<ChatEntity>>>
    suspend fun generateResponse(modelId: String, prompt: String): Flow<Either<String, Unit>>
    suspend fun clearChat(modelId: String): Either<String, Unit>
    fun uninitializeLlmInferece(): Flow<Either<String, Unit>>
}
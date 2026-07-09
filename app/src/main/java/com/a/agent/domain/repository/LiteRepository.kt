package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.ChatEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface LiteRepository {
    suspend fun initializeEngine(modelId: String, modelPath: File): Flow<Either<String, List<ChatEntity>>>
    suspend fun generateResponse(modelId: String, prompt: String): Flow<Either<String, Unit>>
    suspend fun uninitializeEngine(): Flow<Either<String, Unit>>
}
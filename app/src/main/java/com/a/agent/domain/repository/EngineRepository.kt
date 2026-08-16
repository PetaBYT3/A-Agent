package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.domain.model.Configuration
import com.a.agent.domain.model.ProcessStatus
import kotlinx.coroutines.flow.Flow

interface EngineRepository {
    val isEngineOnline: Flow<Boolean>

    fun getConfiguration(): Flow<Either<String, Pair<Configuration, LlmEntity?>>>
    fun setConfiguration(configuration: Configuration): Flow<Either<String, Unit>>

    fun initializeEngine(llmEntity: LlmEntity): Flow<Either<String, ProcessStatus<String>>>
    fun destroyEngine()

    fun initializeConversation(conversationId: String): Flow<Either<String, Unit>>
    fun destroyConversation()

    fun generateResponse(conversationId: String, prompt: String, image: String?): Flow<Either<String, ProcessStatus<Unit>>>
}
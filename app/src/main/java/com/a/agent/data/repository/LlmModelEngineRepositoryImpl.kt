@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class, FlowPreview::class)

package com.a.agent.data.repository

import arrow.core.Either
import com.a.agent.data.local.AgentDataStore
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.ChatEntity
import com.a.agent.data.local.ConversationEntity
import com.a.agent.domain.model.GenerateState
import com.a.agent.domain.model.InitializeConversationResult
import com.a.agent.domain.model.LlmModelEngineBackend
import com.a.agent.domain.repository.LlmModelEngineRepository
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

class LlmModelEngineRepositoryImpl(
    private val agentDatabase: AgentDatabase,
    private val agentDataStore: AgentDataStore
): LlmModelEngineRepository {
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private val _isLlmModelEngineOnline = MutableStateFlow(false)
    override val isLlmModelEngineOnline: Flow<Boolean> = _isLlmModelEngineOnline.asStateFlow()

    private fun getBackend(llmModelEngineBackend: LlmModelEngineBackend): Backend {
        return when (llmModelEngineBackend) {
            LlmModelEngineBackend.CPU -> Backend.CPU()
            LlmModelEngineBackend.GPU -> Backend.GPU()
            LlmModelEngineBackend.NPU -> Backend.NPU()
        }
    }

    override suspend fun initializeEngine(modelPath: File): Flow<Either<String, Unit>> = flow {
        try {
            val llmModelEngineConfiguration = agentDataStore.llmModelEngineConfiguration.first()

            val engineConfig = EngineConfig(
                modelPath = modelPath.absolutePath,
                backend = getBackend(llmModelEngineConfiguration.processingBackend),
                visionBackend = getBackend(llmModelEngineConfiguration.visionBackend)
            )
            engine = Engine(engineConfig)
            engine?.initialize()
            _isLlmModelEngineOnline.update { true }

            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uninitializeEngine(): Flow<Either<String, Unit>> = flow {
        try {
            conversation?.close()
            engine?.close()
            _isLlmModelEngineOnline.update { false }
            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getConversations(): Flow<Either<String, List<ConversationEntity>>> = flow {
        try {
            agentDatabase.conversationDao.getConversations().collect { conversationEntities ->
                emit(Either.Right(conversationEntities))
            }
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsertConversation(conversationEntity: ConversationEntity): Flow<Either<String, Unit>> = flow {
        try {
            agentDatabase.conversationDao.upsertConversation(conversationEntity)
            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun initializeConversation(conversationId: String): Flow<Either<String, InitializeConversationResult>> = flow {
        try {
            val initialMessages = agentDatabase.chatDao.getChats(conversationId).first().takeLast(10).map { chatEntity ->
                when (chatEntity.fromUser) {
                    true -> Message.user(chatEntity.chat)
                    false -> Message.model(chatEntity.chat)
                }
            }
            val conversationConfig = ConversationConfig(
                initialMessages = initialMessages
            )
            conversation = engine?.createConversation(conversationConfig)

            val conversationFlow = agentDatabase.conversationDao.getConversation(conversationId)
            val chatFlow = agentDatabase.chatDao.getChats(conversationId)

            val combinedFlow = conversationFlow.combine(chatFlow) { conversationEntity, chatEntities ->
                if (conversationEntity != null) {
                    val initializeConversationResult = InitializeConversationResult(
                        conversation = conversationEntity,
                        chatList = chatEntities
                    )
                    Either.Right(initializeConversationResult)
                } else {
                    Either.Left("Conversation Unavailable")
                }
            }
            emitAll(combinedFlow)
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uninitializeConversation(): Flow<Either<String, Unit>> = flow {
        try {
            conversation?.close()
            conversation = null
            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun generateResponse(conversationId: String, prompt: String): Flow<Either<String, GenerateState>> = flow {
        try {
            val activeConversation = conversation ?: throw IllegalStateException("Engine not initialized")

            val userChat = ChatEntity(
                conversationId = conversationId,
                fromUser = true,
                chat = prompt
            )
            agentDatabase.chatDao.upsertChat(userChat)
            emit(Either.Right(GenerateState.Requested))

            val fullResponse = StringBuilder()
            activeConversation.sendMessageAsync(prompt).collect { message ->
                fullResponse.append(message.toString())
                emit(Either.Right(GenerateState.Generating))
            }

            val modelChat = ChatEntity(
                conversationId = conversationId,
                fromUser = false,
                chat = fullResponse.toString()
            )
            agentDatabase.chatDao.upsertChat(modelChat)
            emit(Either.Right(GenerateState.Generated))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}
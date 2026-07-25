@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class, FlowPreview::class)

package com.a.agent.data.repository

import android.app.Application
import android.os.Process
import androidx.room.withTransaction
import arrow.core.Either
import com.a.agent.data.local.AgentDataStore
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.ChatEntity
import com.a.agent.data.local.ConversationEntity
import com.a.agent.domain.model.GenerateState
import com.a.agent.domain.model.LlmModelEngineBackend
import com.a.agent.domain.repository.LlmModelEngineRepository
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LlmModelEngineRepositoryImpl(
    private val application: Application,
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
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
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
            delay(2.seconds)
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

    override suspend fun deleteConversation(conversationEntity: ConversationEntity): Flow<Either<String, String>> = flow {
        try {
            val currentChats = agentDatabase.chatDao.getChats(conversationEntity.id).first()

            agentDatabase.withTransaction {
                agentDatabase.conversationDao.deleteConversation(conversationEntity.id)
                agentDatabase.chatDao.clearChat(conversationEntity.id)
            }
            currentChats.mapNotNull { it.imagePath }.forEach { imagePath ->
                val imageFile = File(imagePath)
                if (imageFile.exists()) imageFile.delete()
            }

            emit(Either.Right("All Conversation Data Successfully Deleted"))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun initializeConversation(conversationId: String): Flow<Either<String, Pair<ConversationEntity, List<ChatEntity>>>> = flow {
        try {
            if (_isLlmModelEngineOnline.value) {
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
            }

            val conversationFlow = agentDatabase.conversationDao.getConversation(conversationId)
            val chatFlow = agentDatabase.chatDao.getChats(conversationId)

            val combinedFlow = conversationFlow.combine(chatFlow) { conversationEntity, chatEntities ->
                val pair = Pair(
                    first = conversationEntity ?: ConversationEntity.Empty,
                    second = chatEntities
                )
                Either.Right(pair)
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

    override suspend fun generateResponse(conversationId: String, imageInput: String?, prompt: String): Flow<Either<String, GenerateState>> = flow {
        try {
            val activeConversation = conversation ?: throw IllegalStateException("Engine not initialized")

            val chatId = Uuid.random().toString()
            val targetImagePath = File(application.getExternalFilesDir(null), "images" + File.separator + "$chatId.jpg")

            if (imageInput != null) {
                val parentDir = targetImagePath.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs()
                }
                val imageBytes = File(imageInput).readBytes()
                targetImagePath.writeBytes(imageBytes)
            }

            val userChat = ChatEntity(
                id = chatId,
                conversationId = conversationId,
                fromUser = true,
                imagePath = if (imageInput != null) targetImagePath.absolutePath else null,
                chat = prompt
            )
            agentDatabase.chatDao.upsertChat(userChat)
            emit(Either.Right(GenerateState.Requested))

            val fullResponse = StringBuilder()
            val generatedResponseFlow = if (imageInput != null) {
                val promptWithImage = Contents.of(
                    Content.ImageBytes(File(imageInput).readBytes()),
                    Content.Text(prompt),
                )
                activeConversation.sendMessageAsync(promptWithImage)
            } else {
                activeConversation.sendMessageAsync(prompt)
            }

            generatedResponseFlow.collect { message ->
                fullResponse.append(message.toString())
                emit(Either.Right(GenerateState.Generating))
            }

            val modelChat = ChatEntity(
                conversationId = conversationId,
                fromUser = false,
                imagePath = null,
                chat = fullResponse.toString()
            )
            agentDatabase.chatDao.upsertChat(modelChat)
            emit(Either.Right(GenerateState.Generated))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)
}
@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class, FlowPreview::class)

package com.a.agent.data.repository

import arrow.core.Either
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.ChatEntity
import com.a.agent.data.util.safeExecute
import com.a.agent.domain.repository.LiteRepository
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LiteRepositoryImpl(
    private val agentDatabase: AgentDatabase
): LiteRepository {
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    override suspend fun initializeEngine(modelId: String, modelPath: File): Flow<Either<String, List<ChatEntity>>> {
        return safeExecute {
            val engineConfig = EngineConfig(
                modelPath = modelPath.absolutePath,
                backend = Backend.GPU(),
                visionBackend = Backend.GPU()
            )
            engine = Engine(engineConfig)
            engine?.initialize()

            val chatHistory = agentDatabase.chatDao.getChats(modelId).first()
            val initialMessage = chatHistory.sortedBy { it.timeStamp }.takeLast(10).map { chatEntities ->
                when (chatEntities.fromUser) {
                    true -> Message.user(chatEntities.chat)
                    false -> Message.model(chatEntities.chat)
                }
            }
            val conversationConfig = ConversationConfig(
                initialMessages = initialMessage
            )
            conversation = engine?.createConversation(conversationConfig)

            agentDatabase.chatDao.getChats(modelId).collect { chatEntities ->
                emit(chatEntities)
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun generateResponse(
        modelId: String,
        prompt: String
    ): Flow<Either<String, Unit>> {
        return safeExecute {
            val activeConversation = conversation ?: throw IllegalStateException("Engine not initialized")

            val userChat = ChatEntity(
                modelId = modelId,
                fromUser = true,
                chat = prompt
            )
            agentDatabase.chatDao.upsertChat(userChat)

            val modelChatId = Uuid.random().toString()
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val fullResponse = StringBuilder()
            activeConversation.sendMessageAsync(prompt).collect { message ->
                fullResponse.append(message.toString())
                val modelChat = ChatEntity(
                    id = modelChatId,
                    modelId = modelId,
                    fromUser = false,
                    chat = fullResponse.toString(),
                    timeStamp = timeStamp
                )
                agentDatabase.chatDao.upsertChat(modelChat)
            }
            emit(Unit)
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun uninitializeEngine(): Flow<Either<String, Unit>> {
        return safeExecute {
            conversation?.close()
            engine?.close()

            conversation = null
            engine = null
            emit(Unit)
        }.flowOn(Dispatchers.IO)
    }
}
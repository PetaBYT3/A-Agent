@file:OptIn(ExperimentalCoroutinesApi::class)

package com.a.agent.data.repository

import arrow.core.Either
import com.a.agent.data.local.DataStore
import com.a.agent.data.local.Database
import com.a.agent.data.local.chat.ChatEntity
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.mapper.toMessage
import com.a.agent.domain.model.Configuration
import com.a.agent.domain.model.Directory
import com.a.agent.domain.model.LlmBackend
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.DirectoryRepository
import com.a.agent.domain.repository.EngineRepository
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.uuid.Uuid

class EngineRepositoryImpl(
    private val dataStore: DataStore,
    private val database: Database,
    private val directoryRepository: DirectoryRepository
): EngineRepository {
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private val _isEngineOnline = MutableStateFlow(false)
    override val isEngineOnline: Flow<Boolean> = _isEngineOnline.asStateFlow()

    override fun getConfiguration(): Flow<Either<String, Pair<Configuration, LlmEntity?>>> {
        return flow<Either<String, Pair<Configuration, LlmEntity?>>> {
            val configuration = dataStore.configuration
            val llmEntity = configuration.flatMapLatest { configuration ->
                database.llmDao.getLlm(configuration.selectedLlmId)
            }
            val pair = configuration.combine(llmEntity) { configuration, llmEntity ->
                Either.Right(Pair(configuration, llmEntity))
            }
            emitAll(pair)
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun setConfiguration(configuration: Configuration): Flow<Either<String, Unit>> {
        return flow<Either<String, Unit>> {
            dataStore.setLlmModelEngineConfiguration(configuration)
            emit(Either.Right(Unit))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }
    }

    override fun initializeEngine(llmEntity: LlmEntity): Flow<Either<String, ProcessStatus<String>>> {
        return flow<Either<String, ProcessStatus<String>>> {
            if (engine != null) {
                emit(Either.Right(ProcessStatus.OnCompletion))
                return@flow
            }

            emit(Either.Right(ProcessStatus.OnProcess("Initialize Engine...")))
            val configuration = dataStore.configuration.first()
            val engineConfig = EngineConfig(
                modelPath = llmEntity.path,
                backend = configuration.processing.getBackend()
            )
            engine = Engine(engineConfig)
            engine?.initialize()

            emit(Either.Right(ProcessStatus.OnProcess("Initialize Conversation...")))
            val conversationConfig = ConversationConfig(
                initialMessages = emptyList()
            )
            conversation = engine?.createConversation(conversationConfig)
            _isEngineOnline.update { true }
            emit(Either.Right(ProcessStatus.OnCompletion))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun destroyEngine() {
        conversation?.close()
        conversation = null

        engine?.close()
        engine = null
        _isEngineOnline.update { false }
    }

    override fun initializeConversation(conversationId: String): Flow<Either<String, Unit>> {
        return flow {
            if (engine == null) {
                emit(Either.Left("Engine Offline"))
                return@flow
            }

            val initialMessage = database.chatDao.getChats(conversationId).first().takeLast(10).map { chatEntity ->
                if (chatEntity.fromUser) {
                    Message.user(chatEntity.chat)
                } else {
                    Message.model(chatEntity.chat)
                }
            }
            val conversationConfig = ConversationConfig(
                initialMessages = initialMessage
            )
            conversation = engine?.createConversation(conversationConfig)
            emit(Either.Right(Unit))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun destroyConversation() {
        conversation?.close()
        conversation = null
    }

    override fun generateResponse(conversationId: String, prompt: String, image: String?): Flow<Either<String, ProcessStatus<Unit>>> {
        return flow {
            val currentConversation = conversation
            if (currentConversation == null) {
                emit(Either.Left("Conversation Destroyed"))
                return@flow
            }

            val chatId = Uuid.random().toString()
            if (image != null) {
                val targetFile = directoryRepository.setDirectory(Directory.Image, "$chatId.jpg")
                File(image).copyTo(targetFile, overwrite = true)
            }
            val userChat = ChatEntity(
                id = chatId,
                conversationId = conversationId,
                fromUser = true,
                imagePath = image,
                chat = prompt
            )
            database.chatDao.upsertChat(userChat)
            emit(Either.Right(ProcessStatus.OnProcess(Unit)))

            val fullResponse = StringBuilder()
            val contents = if (image != null) {
                Contents.of(
                    Content.ImageFile(image),
                    Content.Text(prompt),
                )
            } else {
                Contents.of(
                    Content.Text(prompt)
                )
            }
            currentConversation.sendMessageAsync(contents).collect { message ->
                fullResponse.append(message.toString())
            }
            val llmChat = ChatEntity(
                conversationId = conversationId,
                fromUser = false,
                imagePath = null,
                chat = fullResponse.toString(),
            )
            database.chatDao.upsertChat(llmChat)
            emit(Either.Right(ProcessStatus.OnCompletion))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    private fun LlmBackend.getBackend(): Backend {
        return when (this) {
            LlmBackend.CPU -> Backend.CPU()
            LlmBackend.GPU -> Backend.GPU()
            LlmBackend.NPU -> Backend.NPU()
        }
    }
}
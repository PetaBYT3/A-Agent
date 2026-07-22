@file:OptIn(ExperimentalCoroutinesApi::class)

package com.a.agent.data.repository

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.room.withTransaction
import arrow.core.Either
import com.a.agent.data.local.AgentDataStore
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.data.remote.ModelApi
import com.a.agent.data.remote.ModelMetadataDto
import com.a.agent.domain.model.LlmModelEngineConfiguration
import com.a.agent.domain.model.LlmModelFilter
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.presentation.util.toMegaByte
import com.a.agent.service.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class LlmModelManagerRepositoryImpl(
    private val application: Application,
    private val modelApi: ModelApi,
    private val agentDatabase: AgentDatabase,
    private val agentDataStore: AgentDataStore
): LlmModelManagerRepository {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun getLlmModelEngineConfiguration(): Flow<Either<String, Pair<LlmModelEngineConfiguration, ModelEntity>>> = channelFlow {
        try {
            agentDataStore.llmModelEngineConfiguration.collectLatest { llmModelEngineConfiguration ->
                if (llmModelEngineConfiguration.selectedModelId.isBlank()) {
                    send(Either.Left("No Model Selected"))
                }

                agentDatabase.modelDao.getModel(llmModelEngineConfiguration.selectedModelId).collect { modelEntity ->
                    send(Either.Right(Pair(llmModelEngineConfiguration, modelEntity ?: ModelEntity.Empty)))
                }
            }
        } catch (e: Exception) {
            send(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsertLlmModelEngineConfiguration(llmModelEngineConfiguration: LlmModelEngineConfiguration): Flow<Either<String, String>> = flow {
        try {
            agentDataStore.setLlmModelEngineConfiguration(llmModelEngineConfiguration)
            emit(Either.Right("Model Engine Configuration Updated"))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getSelectedModel(): Flow<Either<String, ModelEntity>> = flow {
        try {
            agentDataStore.llmModelEngineConfiguration.flatMapLatest { llmModelEngineConfiguration ->
                if (llmModelEngineConfiguration.selectedModelId.isNotBlank()) {
                    agentDatabase.modelDao.getModel(llmModelEngineConfiguration.selectedModelId)
                } else {
                    throw Exception("No Model Selected")
                }
            }.collect { modelEntity ->
                if (modelEntity != null) {
                    emit(Either.Right(modelEntity))
                } else {
                    emit(Either.Right(ModelEntity.Empty))
                }
            }
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getModels(llmModelFilter: LlmModelFilter): Flow<Either<String, List<ModelEntity>>> = flow {
        try {
            agentDatabase.modelDao.getModels().collect { modelEntities ->
                when (llmModelFilter) {
                    LlmModelFilter.All -> {
                        emit(Either.Right(modelEntities))
                    }
                    LlmModelFilter.RequestDownload -> {
                        val requestDownloadLlmModel = modelEntities.filter { modelEntity ->
                            File(modelEntity.path).length().toMegaByte() != modelEntity.totalBytes.toMegaByte()
                        }
                        emit(Either.Right(requestDownloadLlmModel))
                    }
                    LlmModelFilter.Downloaded -> {
                        val downloadedLlmModel = modelEntities.filter { modelEntity ->
                            File(modelEntity.path).length().toMegaByte() == modelEntity.totalBytes.toMegaByte()
                        }
                        emit(Either.Right(downloadedLlmModel))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getModel(modelId: String): Flow<Either<String, ModelEntity>> = flow {
        try {
            agentDatabase.modelDao.getModel(modelId).collect { modelEntity ->
                if (modelEntity != null) {
                    emit(Either.Right(modelEntity))
                } else {
                    emit(Either.Right(ModelEntity.Empty))
                }
            }
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun upsertModel(modelEntity: ModelEntity): Flow<Either<String, Unit>> = flow {
        try {
            agentDatabase.modelDao.upsertModel(modelEntity)
            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteModel(modelEntity: ModelEntity): Flow<Either<String, Unit>> = flow {
        try {
            agentDatabase.withTransaction {
                val modelFile = File(modelEntity.path)
                if (modelFile.exists()) modelFile.delete()
                agentDatabase.modelDao.deleteModel(modelEntity.id)
            }
            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getModelMetadata(url: String): Flow<Either<String, ModelMetadataDto>> = flow {
        try {
            val modelMetadataDto = modelApi.getModelMetadata(url)
            emit(Either.Right(modelMetadataDto))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    private val activeDownloadJob = ConcurrentHashMap<String, Job>()
    private val _activeDownloadInfo = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
    override val activeDownloadInfo = _activeDownloadInfo.asStateFlow()

    override suspend fun toggleDownload(modelEntity: ModelEntity): Flow<Either<String, Unit>> = flow {
        try {
            val serviceIntent = Intent(application, DownloadService::class.java)

            val downloadJob = activeDownloadJob[modelEntity.id]
            if (downloadJob != null && downloadJob.isActive) {
                activeDownloadJob.remove(modelEntity.id)?.cancel()
                _activeDownloadInfo.update { it - modelEntity.id }
                stopService(serviceIntent)

                emit(Either.Right(Unit))
                return@flow
            }

            ContextCompat.startForegroundService(application, serviceIntent)
            activeDownloadJob[modelEntity.id] = applicationScope.launch {
                try {
                    _activeDownloadInfo.update {
                        it + (modelEntity.id to DownloadInfo(modelEntity.totalBytes, 0, 0f, 0))
                    }

                    modelApi.getModelFile(modelEntity.url, File(modelEntity.path)).collect { downloadInfo ->
                        _activeDownloadInfo.update { it + (modelEntity.id to downloadInfo) }
                    }
                    _activeDownloadInfo.update { it - modelEntity.id }
                    stopService(serviceIntent)
                } catch (e: Exception) {
                    emit(Either.Left(e.message ?: "Unknown Error"))
                }
            }

            emit(Either.Right(Unit))
        } catch (e: Exception) {
            emit(Either.Left(e.message ?: "Unknown Error"))
        }
    }

    private fun stopService(serviceIntent: Intent) {
        if (activeDownloadJob.isEmpty()) application.stopService(serviceIntent)
    }
}
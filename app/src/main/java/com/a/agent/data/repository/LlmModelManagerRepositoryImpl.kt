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
import com.a.agent.domain.model.LlmModelFilter
import com.a.agent.domain.repository.LlmModelManagerRepository
import com.a.agent.service.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
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

    override suspend fun getSelectedModel(): Flow<Either<String, ModelEntity>> = flow {
        try {
            agentDataStore.selectedModelId.flatMapLatest { modelId ->
                if (modelId != null) {
                    agentDatabase.modelDao.getModel(modelId)
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

    override suspend fun setSelectedModel(modelId: String): Flow<Either<String, Unit>> = flow {
        try {
            agentDataStore.setSelectedModelId(modelId)
            emit(Either.Right(Unit))
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
                            !File(modelEntity.path).exists()
                        }
                        emit(Either.Right(requestDownloadLlmModel))
                    }
                    LlmModelFilter.Downloaded -> {
                        val downloadedLlmModel = modelEntities.filter { modelEntity ->
                            File(modelEntity.path).exists()
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
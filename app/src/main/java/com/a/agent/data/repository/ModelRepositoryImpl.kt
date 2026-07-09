package com.a.agent.data.repository

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.room.withTransaction
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.a.agent.data.local.AgentDatabase
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.data.remote.ModelApi
import com.a.agent.data.remote.ModelMetadataDto
import com.a.agent.data.util.UnavailableDataException
import com.a.agent.data.util.simulateDownload
import com.a.agent.domain.repository.ModelRepository
import com.a.agent.service.DownloadService
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ModelRepositoryImpl(
    private val application: Application,
    private val modelApi: ModelApi,
    private val agentDatabase: AgentDatabase
): ModelRepository {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun getModels(): Flow<Either<String, List<ModelEntity>>> {
        return agentDatabase.modelDao.getModels().map { modelEntities ->
            modelEntities.right()
        }.catch { it.message.left() }
    }

    override suspend fun getModel(modelId: String): Flow<Either<String, ModelEntity>> {
        return agentDatabase.modelDao.getModel(modelId).map { modelEntity ->
            modelEntity?.right() ?: throw UnavailableDataException("Data not found")
        }.catch { it.message.left() }
    }

    override suspend fun upsertModel(modelEntity: ModelEntity): Either<String, Unit> {
        return Either.catch {
            agentDatabase.modelDao.upsertModel(modelEntity)
        }.mapLeft { it.message ?: "Unknown Error" }
    }

    override suspend fun deleteModel(modelEntity: ModelEntity): Either<String, Unit> {
        return Either.catch {
            agentDatabase.withTransaction {
                agentDatabase.modelDao.deleteModel(modelEntity.id)
                val modelPath = modelEntity.path
                if (modelPath.exists()) {
                    modelPath.delete()
                }
            }
        }.mapLeft { it.message ?: "Unknown Error" }
    }

    override suspend fun getModelMetadata(url: String): Either<String, ModelMetadataDto> {
        return Either.catch {
            modelApi.getModelMetadata(url)
        }.mapLeft { it.message ?: "Unknown Error" }
    }

    private val downloadJob = ConcurrentHashMap<String, Job>()

    private val _downloadState = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
    override val downloadState = _downloadState.asStateFlow()

    override suspend fun toggleDownload(modelEntity: ModelEntity): Either<String, Unit> {
        val serviceIntent = Intent(application, DownloadService::class.java)

        val currentJob = downloadJob[modelEntity.id]
        if (currentJob != null && currentJob.isActive) {
            currentJob.cancel()
            downloadJob.remove(modelEntity.id)
            _downloadState.update { it - modelEntity.id }
            if (downloadJob.isEmpty()) application.stopService(serviceIntent)
            return Unit.right()
        }

        ContextCompat.startForegroundService(application, serviceIntent)
        downloadJob[modelEntity.id] = applicationScope.launch {
            _downloadState.update { it + (modelEntity.id to DownloadInfo(modelEntity.totalBytes, 0, 0f, 0))}
            modelApi.getModelFile(modelEntity.url, modelEntity.path).collect { downloadInfo ->
                _downloadState.update { it + (modelEntity.id to downloadInfo) }
            }
            _downloadState.update { it - modelEntity.id }
            if (downloadJob.isEmpty()) application.stopService(serviceIntent)
        }

        return Unit.right()
    }
}
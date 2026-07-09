package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.data.remote.ModelMetadataDto
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    suspend fun getModels(): Flow<Either<String, List<ModelEntity>>>
    suspend fun getModel(modelId: String): Flow<Either<String, ModelEntity>>
    suspend fun upsertModel(modelEntity: ModelEntity): Either<String, Unit>
    suspend fun deleteModel(modelEntity: ModelEntity): Either<String, Unit>

    suspend fun getModelMetadata(url: String): Either<String, ModelMetadataDto>

    val downloadState: Flow<Map<String, DownloadInfo>>
    suspend fun toggleDownload(modelEntity: ModelEntity): Either<String, Unit>
}
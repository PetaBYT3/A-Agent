package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.data.remote.ModelMetadataDto
import com.a.agent.domain.model.LlmModelFilter
import kotlinx.coroutines.flow.Flow

interface LlmModelManagerRepository {
    suspend fun getSelectedModel(): Flow<Either<String, ModelEntity>>
    suspend fun setSelectedModel(modelId: String): Flow<Either<String, Unit>>

    suspend fun getModels(llmModelFilter: LlmModelFilter = LlmModelFilter.All): Flow<Either<String, List<ModelEntity>>>
    suspend fun getModel(modelId: String): Flow<Either<String, ModelEntity>>
    suspend fun upsertModel(modelEntity: ModelEntity): Flow<Either<String, Unit>>
    suspend fun deleteModel(modelEntity: ModelEntity): Flow<Either<String, Unit>>

    suspend fun getModelMetadata(url: String): Flow<Either<String, ModelMetadataDto>>
    val activeDownloadInfo: Flow<Map<String, DownloadInfo>>
    suspend fun toggleDownload(modelEntity: ModelEntity): Flow<Either<String, Unit>>
}
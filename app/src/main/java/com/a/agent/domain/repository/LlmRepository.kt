package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.domain.model.LlmMetadata
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LlmRepository {
    suspend fun validateSelectedLlm()

    fun getMetadataFromUrl(url: String): Flow<Either<String, LlmMetadata>>
    fun getMetadataFromLocal(platformFile: PlatformFile): Flow<Either<String, LlmMetadata>>

    fun getLlm(id: String): Flow<Either<String, LlmEntity?>>
    fun getLlms(llmSource: LlmSource? = null): Flow<Either<String, List<LlmEntity>>>
    fun upsertLlm(llmEntity: LlmEntity): Flow<Either<String, String>>
    fun deleteLlm(llmEntity: LlmEntity): Flow<Either<String, String>>

    fun copyFromLocal(platformFile: PlatformFile): Flow<Either<String, Unit>>

    val activeDownloadMap: StateFlow<Map<String, Pair<String, DownloadInfo>>>
    fun toggleDownload(llmEntity: LlmEntity): Flow<Either<String, String>>
    fun deleteOrphanFile(): Flow<Either<String, String>>
}
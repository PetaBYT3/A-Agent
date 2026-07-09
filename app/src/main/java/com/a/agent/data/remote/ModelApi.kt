package com.a.agent.data.remote

import kotlinx.coroutines.flow.Flow
import java.io.File

interface ModelApi {
    suspend fun getModelMetadata(url: String): ModelMetadataDto
    suspend fun getModelFile(url: String, path: File): Flow<DownloadInfo>
}
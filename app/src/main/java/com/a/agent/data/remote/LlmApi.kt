package com.a.agent.data.remote

import kotlinx.coroutines.flow.Flow

interface LlmApi {
    suspend fun getLlmMetadata(url: String): LlmMetadataDto
    suspend fun getLlmFile(url: String, path: String): Flow<DownloadInfo>
}
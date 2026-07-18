package com.a.agent.domain.usecase.model

import com.a.agent.data.remote.DownloadInfo
import com.a.agent.domain.repository.LlmModelManagerRepository
import kotlinx.coroutines.flow.Flow

class DownloadState(
    private val llmModelManagerRepository: LlmModelManagerRepository
) {
    operator fun invoke(): Flow<Map<String, DownloadInfo>> {
        return llmModelManagerRepository.activeDownloadInfo
    }
}
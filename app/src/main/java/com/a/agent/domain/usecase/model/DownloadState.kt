package com.a.agent.domain.usecase.model

import com.a.agent.data.remote.DownloadInfo
import com.a.agent.domain.repository.ModelRepository
import kotlinx.coroutines.flow.Flow

class DownloadState(
    private val modelRepository: ModelRepository
) {
    operator fun invoke(): Flow<Map<String, DownloadInfo>> {
        return modelRepository.downloadState
    }
}
package com.a.agent.presentation.model

import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo

data class ModelState(
    val modelEntities: List<ModelEntity> = emptyList(),
    val downloadState: Map<String, DownloadInfo> = emptyMap(),
    val isDownloadInitialing: Boolean = false
)

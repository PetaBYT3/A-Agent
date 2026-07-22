package com.a.agent.presentation.model

import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo

data class ModelState(
    val isDownloadedModelLoading: Boolean = true,
    val downloadedModelEntities: List<ModelEntity> = emptyList(),
    val downloadedModelError: String? = null,

    val isRequireDownloadModelLoading: Boolean = true,
    val requireDownloadModelEntities: List<ModelEntity> = emptyList(),
    val requireDownloadModelError: String? = null,

    val downloadState: Map<String, DownloadInfo> = emptyMap()
)

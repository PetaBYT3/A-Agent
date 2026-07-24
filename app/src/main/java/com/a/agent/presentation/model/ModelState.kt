package com.a.agent.presentation.model

import com.a.agent.data.local.ModelEntity
import com.a.agent.data.remote.DownloadInfo

data class ModelState(
    val downloadState: Map<String, DownloadInfo> = emptyMap(),

    val isAllModelsLoading: Boolean = false,
    val isAllModelsError: String? = null,
    val allModels: List<ModelEntity> = emptyList(),

    val isDefaultModelsLoading: Boolean = false,
    val isDefaultModelsError: String? = null,
    val defaultModels: List<ModelEntity> = emptyList(),

    val isUrlModelsLoading: Boolean = false,
    val isUrlModelsError: String? = null,
    val urlModels: List<ModelEntity> = emptyList(),

    val isLocalModelsLoading: Boolean = false,
    val isLocalModelsError: String? = null,
    val localModels: List<ModelEntity> = emptyList()
)

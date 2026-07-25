package com.a.agent.presentation.modelmanager

import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelSource

data class ModelManagerState(
    val isOnEdit: Boolean = false,

    val isModelLoading: Boolean = true,
    val isModelError: String? = null,
    val model: ModelEntity = ModelEntity.Empty.copy(modelSource = ModelSource.Url),

    val isMetadataLoading: Boolean = false,
    val isMetadataError: String? = null,
    val isModelSupported: Boolean = false,
)

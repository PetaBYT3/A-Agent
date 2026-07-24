package com.a.agent.presentation.modelmanager

import com.a.agent.data.local.ModelEntity

data class ModelManagerState(
    val isOnEdit: Boolean = false,

    val isModelLoading: Boolean = true,
    val isModelError: String? = null,
    val model: ModelEntity = ModelEntity.Empty,

    val isMetadataLoading: Boolean = false,
    val isMetadataError: String? = null,
    val isModelSupported: Boolean = false,
)

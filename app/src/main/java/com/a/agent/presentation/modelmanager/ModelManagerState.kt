package com.a.agent.presentation.modelmanager

import com.a.agent.data.local.ModelEntity

data class ModelManagerState(
    val isOnEdit: Boolean = false,
    val modelEntityToEdit: ModelEntity = ModelEntity.Empty,

    val nameTextField: String = "",

    val isMetadataLoading: Boolean = false,
    val isMetadataError: String? = null,

    val fileName: String = "",
    val totalBytes: Long = 0,
    val isSupported: Boolean = false,

    val urlTextField: String = "",
)

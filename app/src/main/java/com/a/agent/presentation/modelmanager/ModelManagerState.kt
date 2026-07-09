package com.a.agent.presentation.modelmanager

import com.a.agent.data.local.ModelType

data class ModelManagerState(
    val isOnEdit: Boolean = false,

    val urlTextField: String = "",
    val nameTextField: String = "",
    val typeRadioButton: ModelType? = null,
    val fileName: String = "",
    val totalBytes: Long = 0,
    val isSupported: Boolean = false,

    val isMetadataLoading: Boolean = false,
    val isMetadataError: String? = null,
)

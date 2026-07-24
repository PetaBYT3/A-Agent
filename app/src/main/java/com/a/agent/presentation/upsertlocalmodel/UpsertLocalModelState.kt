package com.a.agent.presentation.upsertlocalmodel

import com.a.agent.data.local.ModelEntity
import io.github.vinceglb.filekit.PlatformFile

data class UpsertLocalModelState(
    val isOnEdit: Boolean = false,

    val isModelLoading: Boolean = false,
    val isModelError: String? = null,
    val model: ModelEntity = ModelEntity.Empty,

    val modelPlatformFile: PlatformFile? = null,
    val isModelSupported: Boolean = false,

    val isUpsertModelLoading: Boolean = false
)

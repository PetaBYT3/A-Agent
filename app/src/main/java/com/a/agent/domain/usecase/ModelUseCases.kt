package com.a.agent.domain.usecase

import com.a.agent.domain.usecase.model.DeleteModel
import com.a.agent.domain.usecase.model.DownloadState
import com.a.agent.domain.usecase.model.GetModel
import com.a.agent.domain.usecase.model.GetModelMetadata
import com.a.agent.domain.usecase.model.GetModels
import com.a.agent.domain.usecase.model.ToggleDownload
import com.a.agent.domain.usecase.model.UpsertModel

data class ModelUseCases(
    val getModels: GetModels,
    val getModel: GetModel,
    val upsertModel: UpsertModel,
    val deleteModel: DeleteModel,
    val getModelMetadata: GetModelMetadata,
    val downloadState: DownloadState,
    val toggleDownload: ToggleDownload
)
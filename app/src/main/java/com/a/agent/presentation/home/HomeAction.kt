package com.a.agent.presentation.home

import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.LlmModelEngineBackend

sealed interface HomeAction {
    data object DownloadedModelBottomSheet: HomeAction
    data class SelectModel(val modelEntity: ModelEntity): HomeAction

    data object LlmModelEngineConfigurationBottomSheet: HomeAction
    data class ProcessBackendChip(val backend: LlmModelEngineBackend): HomeAction
    data class VisionBackendChip(val backend: LlmModelEngineBackend): HomeAction
    data class MaxNumTokens(val tokens: Int): HomeAction

    data object ToggleModelEngine: HomeAction

    data object UpsertConversationBottomSheet: HomeAction
    data class ConversationNameTextField(val name: String): HomeAction
    data object UpsertConversationButton: HomeAction
}
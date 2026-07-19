package com.a.agent.presentation.home

import com.a.agent.data.local.ConversationEntity
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.LlmModelEngineConfiguration

data class HomeState(
    val llmModelEngineConfiguration: LlmModelEngineConfiguration? = null,
    val selectedModelEntity: ModelEntity = ModelEntity.Empty,

    val initializeError: String? = null,

    val downloadedModelBottomSheet: Boolean = false,
    val downloadedModelEntities: List<ModelEntity> = emptyList(),
    val downloadedModelError: String? = null,

    val isModelEngineOnline: Boolean = false,
    val isModelEngineLoading: Boolean = false,

    val conversationEntities: List<ConversationEntity> = emptyList(),
    val conversationError: String? = null,

    val upsertConversationBottomSheet: Boolean = false,
    val conversationNameTextField: String = ""
)

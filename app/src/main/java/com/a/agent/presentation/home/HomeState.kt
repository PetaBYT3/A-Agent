package com.a.agent.presentation.home

import com.a.agent.data.local.ConversationEntity
import com.a.agent.data.local.ModelEntity

data class HomeState(
    val selectedModelEntity: ModelEntity = ModelEntity.Empty,
    val selectedModelError: String? = null,

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

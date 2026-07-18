package com.a.agent.presentation.home

import com.a.agent.data.local.ModelEntity

sealed interface HomeAction {
    data object ToggleModelEngine: HomeAction

    data object DownloadedModelBottomSheet: HomeAction
    data class SelectModel(val modelEntity: ModelEntity): HomeAction

    data object UpsertConversationBottomSheet: HomeAction
    data class ConversationNameTextField(val name: String): HomeAction
    data object UpsertConversationButton: HomeAction
}
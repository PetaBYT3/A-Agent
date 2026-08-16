package com.a.agent.presentation.home

import com.a.agent.data.local.conversation.ConversationDetailEntity
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.domain.model.Configuration

data class HomeState(
    val isNotificationPermissionGranted: Boolean = true,
    val isStoragePermissionGranted: Boolean = true,
    val isMicrophonePermissionGranted: Boolean = true,

    val isEngineOnline: Boolean = false,
    val engineTitle: String = "",
    val engineStatus: String = "",
    val isEngineLoading: Boolean = false,

    val isConfigurationLoading: Boolean = true,
    val isConfigurationError: String? = null,
    val isConfigurationBottomSheetVisible: Boolean = false,
    val configuration: Configuration = Configuration.Empty,
    val selectedLlm: LlmEntity? = null,

    val isDownloadedLlmBottomSheetVisible: Boolean = false,
    val isDownloadedLlmError: String? = null,
    val downloadedLlm: List<LlmEntity> = emptyList(),

    val isConversationsLoading: Boolean = true,
    val isConversationsError: String? = null,
    val conversationDetails: List<ConversationDetailEntity> = emptyList(),
)

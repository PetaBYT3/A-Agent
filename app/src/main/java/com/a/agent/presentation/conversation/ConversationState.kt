package com.a.agent.presentation.conversation

import com.a.agent.data.local.chat.ChatEntity
import com.a.agent.data.local.conversation.ConversationEntity
import java.util.Locale

data class ConversationState(
    val isTtsLanguageBottomSheetVisible: Boolean = false,
    val selectedTtsLanguage: Locale = Locale.US,
    val ttsLanguages: List<Locale> = emptyList(),

    val isSttLanguageBottomSheetVisible: Boolean = false,
    val selectedSttLanguage: Locale = Locale.US,
    val sttLanguage: List<Locale> = emptyList(),

    val isDeleteChatBottomSheetVisible: Boolean = false,

    val isEngineConversationLoading: Boolean = true,
    val isEngineConversationError: String? = null,
    val isEngineConversationReady: Boolean = false,

    val isConversationLoading: Boolean = true,
    val isConversationError: String? = null,
    val conversation: ConversationEntity? = null,

    val isChatLoading: Boolean = true,
    val isChatError: String? = null,
    val chats: List<ChatEntity> = emptyList(),

    val isTtsLoading: Boolean = true,
    val isTtsError: String? = null,
    val isTtsRunning: Boolean = false,

    val isMicrophonePermissionGranted: Boolean = false,
    val isMicrophonePermissionDeniedBottomSheetVisible: Boolean = false,
    val isSttRunning: Boolean = false,

    val isGenerating: Boolean = false,

    val imagePicker: String? = null,
    val promptTextField: String = "",
)

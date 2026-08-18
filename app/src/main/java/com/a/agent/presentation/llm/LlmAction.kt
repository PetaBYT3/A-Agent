package com.a.agent.presentation.llm

import com.a.agent.data.local.llm.LlmEntity

sealed interface LlmAction {
    data object DeleteOrphanFileButton: LlmAction
    data object NotificationPermissionDeniedBottomSheet: LlmAction
    data class ToggleDownload(val llmEntity: LlmEntity): LlmAction
}
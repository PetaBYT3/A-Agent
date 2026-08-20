package com.a.agent.presentation.llm

import com.a.agent.data.local.llm.LlmEntity

sealed interface LlmAction {
    data object NotificationPermissionDeniedBottomSheet: LlmAction
    data object DeleteOrphanFileButton: LlmAction

    data object AuthorizationKeyBottomSheet: LlmAction
    data class EnableDefaultAuthorizationKeySwitch(val enabled: Boolean): LlmAction
    data class AuthorizationKeyTextField(val key: String): LlmAction
    data object ButtonSaveAuthorizationKey: LlmAction

    data class ToggleDownload(val llmEntity: LlmEntity): LlmAction
}
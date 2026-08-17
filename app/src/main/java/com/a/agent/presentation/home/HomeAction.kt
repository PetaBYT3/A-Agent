package com.a.agent.presentation.home

import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.domain.model.LlmBackend

sealed interface HomeAction {
    data object UpdatePermission: HomeAction

    data object ToggleEngine: HomeAction

    data object DownloadedLlmBottomSheetVisibility: HomeAction
    data class SelectModel(val llm: LlmEntity): HomeAction

    data object ConfigurationBottomSheetVisibility: HomeAction
    data class AutomaticSwitch(val isAutomatic: Boolean): HomeAction
    data class ProcessBackendChip(val backend: LlmBackend): HomeAction
    data class VisionBackendChip(val backend: LlmBackend): HomeAction
}
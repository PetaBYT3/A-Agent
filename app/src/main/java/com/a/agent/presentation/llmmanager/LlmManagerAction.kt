package com.a.agent.presentation.llmmanager

import com.a.agent.data.local.llm.LlmSource
import io.github.vinceglb.filekit.PlatformFile

sealed interface LlmManagerAction {
    data class LlmSourceChip(val llmSource: LlmSource): LlmManagerAction

    data class UrlTextField(val url: String): LlmManagerAction
    data class LocalFilePicker(val platformFile: PlatformFile): LlmManagerAction

    data class NameTextField(val name: String): LlmManagerAction
    data object DeleteLlmBottomSheet: LlmManagerAction
    data object DeleteLlmButton: LlmManagerAction
    data object UpsertLlmButton: LlmManagerAction
}
package com.a.agent.presentation.modelmanager

import com.a.agent.data.local.ModelSource
import io.github.vinceglb.filekit.PlatformFile

sealed interface ModelManagerAction {
    data class LlmSourceChip(val llmSource: ModelSource): ModelManagerAction

    data class UrlTextField(val url: String): ModelManagerAction
    data class LocalFilePicker(val platformFile: PlatformFile): ModelManagerAction

    data class NameTextField(val name: String): ModelManagerAction
    data object UpsertModel: ModelManagerAction
    data object DeleteModel: ModelManagerAction
}
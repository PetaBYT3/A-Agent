package com.a.agent.presentation.modelmanager

import com.a.agent.data.local.ModelType

sealed interface ModelManagerAction {
    data class UrlTextField(val url: String): ModelManagerAction
    data class NameTextField(val name: String): ModelManagerAction
    data class TypeRadioButton(val type: ModelType): ModelManagerAction
    data object UpsertModel: ModelManagerAction
    data object DeleteModel: ModelManagerAction
}
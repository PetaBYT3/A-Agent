package com.a.agent.presentation.upsertlocalllm

import io.github.vinceglb.filekit.PlatformFile

sealed interface UpsertLocalModelAction {
    data class NameTextField(val name: String): UpsertLocalModelAction

    data class FilePickerButton(val file: PlatformFile?): UpsertLocalModelAction
    data object DeleteModelButton: UpsertLocalModelAction
    data object UpsertModelButton: UpsertLocalModelAction
}
package com.a.agent.presentation.conversation

import io.github.vinceglb.filekit.PlatformFile

sealed interface ConversationAction {
    data object ClearChat: ConversationAction
    data class ImageInputPicker(val image: PlatformFile?): ConversationAction
    data class PromptTextField(val prompt: String): ConversationAction
    data object ImageDialog: ConversationAction
    data object GenerateButton: ConversationAction
}
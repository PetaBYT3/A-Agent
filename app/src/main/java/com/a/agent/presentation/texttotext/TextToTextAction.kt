package com.a.agent.presentation.texttotext

sealed interface TextToTextAction {
    data object ClearChat: TextToTextAction
    data class PromptTextField(val prompt: String): TextToTextAction
    data object GenerateButton: TextToTextAction
}
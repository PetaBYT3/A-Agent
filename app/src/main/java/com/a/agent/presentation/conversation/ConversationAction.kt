package com.a.agent.presentation.conversation

sealed interface ConversationAction {
    data object ClearChat: ConversationAction
    data class PromptTextField(val prompt: String): ConversationAction
    data object GenerateButton: ConversationAction
}
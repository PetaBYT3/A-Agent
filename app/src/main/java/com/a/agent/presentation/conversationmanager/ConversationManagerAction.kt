package com.a.agent.presentation.conversationmanager

sealed interface ConversationManagerAction {
    data class ConversationTitleTextField(val title: String): ConversationManagerAction
    data object UpsertConversationButton: ConversationManagerAction
    data object DeleteConversationBottomSheet: ConversationManagerAction
    data object DeleteConversationButton: ConversationManagerAction
}
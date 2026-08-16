package com.a.agent.presentation.chatconversation

import io.github.vinceglb.filekit.PlatformFile
import java.util.Locale

sealed interface ConversationAction {
    data object TtsLanguageBottomSheet: ConversationAction
    data class SetTtsLanguageButton(val locale: Locale): ConversationAction

    data object SttLanguageBottomSheet: ConversationAction
    data class SetSttLanguageButton(val locale: Locale): ConversationAction

    data object DeleteChatBottomSheet: ConversationAction
    data object DeleteChatButton: ConversationAction

    data class StartTtsButton(val text: String): ConversationAction
    data object StopTtsButton: ConversationAction

    data object MicrophonePermissionDeniedBottomSheet: ConversationAction
    data class ToggleSttButton(val isRunning: Boolean): ConversationAction
    data class ImagePickerButton(val image: PlatformFile?): ConversationAction
    data class PromptTextField(val prompt: String): ConversationAction
    data object GenerateButton: ConversationAction
}
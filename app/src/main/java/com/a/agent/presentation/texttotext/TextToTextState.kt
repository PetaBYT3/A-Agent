package com.a.agent.presentation.texttotext

import com.a.agent.data.local.ChatEntity
import com.a.agent.data.local.ModelEntity

data class TextToTextState(
    val isModelInitializing: Boolean = true,
    val modelEntity: ModelEntity? = null,
    val isError: String? = null,

    val chatList: List<ChatEntity> = emptyList(),
    val isModelThinking: Boolean = false,
    val promptTextField: String = ""
)

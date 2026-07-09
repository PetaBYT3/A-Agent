package com.a.agent.presentation.model

import com.a.agent.data.local.ModelEntity

sealed interface ModelAction {
    data class ToggleDownload(val modelEntity: ModelEntity): ModelAction
}
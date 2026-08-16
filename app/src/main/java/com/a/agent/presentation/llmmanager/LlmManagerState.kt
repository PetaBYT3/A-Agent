package com.a.agent.presentation.llmmanager

import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import io.github.vinceglb.filekit.PlatformFile

data class LlmManagerState(
    val isOnEdit: Boolean = false,

    val isLlmLoading: Boolean = true,
    val isLlmError: String? = null,
    val llm: LlmEntity? = null,

    val llmUrlTextField: String = "",
    val llmLocalPlatformFile: PlatformFile? = null,
    val llmSourceChip: LlmSource = LlmSource.Url,

    val isMetadataLoading: Boolean = false,
    val isMetadataError: String? = null,

    val llmFilePath: String = "",
    val llmNameTextField: String = "",
    val llmFileName: String = "",
    val llmFileSize: Long = 0,
    val isLlmSupported: Boolean = false,

    val isDeleteLlmBottomSheetVisible: Boolean = false,

    val isUpsertLlmButtonLoading: Boolean = false,
)

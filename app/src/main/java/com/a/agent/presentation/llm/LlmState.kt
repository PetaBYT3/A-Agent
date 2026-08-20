package com.a.agent.presentation.llm

import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.remote.DownloadInfo

data class LlmState(
    val isNotificationPermissionGranted: Boolean = false,
    val isNotificationPermissionDeniedBottomSheetVisible: Boolean = false,
    val downloadState: Map<String, Pair<String, DownloadInfo>> = emptyMap(),

    val isAuthorizationKeyBottomSheetVisible: Boolean = false,
    val isEnableDefaultAuthorizationKey: Boolean = true,
    val authorizationKeyTextField: String = "",

    val isAllLlmLoading: Boolean = true,
    val isAllLlmError: String? = null,
    val allLlm: List<LlmEntity> = emptyList(),

    val isDefaultLlmLoading: Boolean = true,
    val isDefaultLlmError: String? = null,
    val defaultLlm: List<LlmEntity> = emptyList(),

    val isUrlLlmLoading: Boolean = true,
    val isUrlLlmError: String? = null,
    val urlLlm: List<LlmEntity> = emptyList(),

    val isLocalLlmLoading: Boolean = true,
    val isLocalLlmError: String? = null,
    val localLlm: List<LlmEntity> = emptyList()
)

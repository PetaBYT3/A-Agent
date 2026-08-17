package com.a.agent.domain.model

data class Configuration(
    val selectedLlmId: String,
    val isAutomatic: Boolean,
    val processing: LlmBackend,
    val vision: LlmBackend
)

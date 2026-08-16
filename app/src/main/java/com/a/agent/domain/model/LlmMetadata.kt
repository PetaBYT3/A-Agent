package com.a.agent.domain.model

data class LlmMetadata(
    val fileName: String,
    val filePath: String,
    val totalBytes: Long,
    val isSupported: Boolean
)

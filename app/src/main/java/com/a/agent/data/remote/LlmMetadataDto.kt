package com.a.agent.data.remote

data class LlmMetadataDto(
    val fileName: String,
    val totalBytes: Long,
    val isSupported: Boolean
)

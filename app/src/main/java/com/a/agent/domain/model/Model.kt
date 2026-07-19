package com.a.agent.domain.model

data class Model(
    val id: String,
    val name: String,
    val url: String,
    val path: String,
    val fileName: String,
    val totalBytes: Long,
    val isDefaultModel: Boolean = false,
    val isDownloaded: Boolean
)

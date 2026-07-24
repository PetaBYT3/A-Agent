package com.a.agent.domain.model

import com.a.agent.data.local.ModelSource

data class Model(
    val id: String,
    val name: String,
    val url: String,
    val path: String,
    val fileName: String,
    val totalBytes: Long,
    val modelSource: ModelSource,
    val isDownloaded: Boolean
)

package com.a.agent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val identifier: String,
    val totalFiles: Int,
    val created: Long
)

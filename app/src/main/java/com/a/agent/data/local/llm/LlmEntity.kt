@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.data.local.llm

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "llmEntity"
)
data class LlmEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val name: String,
    val url: String,
    val path: String,
    val fileName: String,
    val totalBytes: Long,
    val llmSource: LlmSource,
    val isDownloaded: Boolean
) {
    companion object {
        val Empty = LlmEntity(
            id = "",
            name = "",
            url = "",
            path = "",
            fileName = "",
            totalBytes = 0,
            llmSource = LlmSource.Default,
            isDownloaded = false
        )
    }
}

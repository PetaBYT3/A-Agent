@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.a.agent.domain.model.Model
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "modelEntity"
)
data class ModelEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val name: String,
    val url: String,
    val path: String,
    val fileName: String,
    val totalBytes: Long,
    val isDefaultModel: Boolean = false
) {
    companion object {
        val Empty = ModelEntity(
            id = "",
            name = "",
            url = "",
            path = "",
            fileName = "",
            totalBytes = 0,
        )
    }
}

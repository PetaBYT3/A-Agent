@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "modelEntity"
)
data class ModelEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val type: ModelType,
    val name: String,
    val url: String,
    val path: File,
    val fileName: String,
    val totalBytes: Long,
    val isSupported: Boolean,
)

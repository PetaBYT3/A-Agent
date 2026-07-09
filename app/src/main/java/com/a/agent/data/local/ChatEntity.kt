@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.a.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "chatEntity"
)
data class ChatEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val modelId: String,
    val fromUser: Boolean,
    val chat: String,
    val timeStamp: Long = Clock.System.now().toEpochMilliseconds()
)

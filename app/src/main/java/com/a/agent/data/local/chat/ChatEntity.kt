@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package com.a.agent.data.local.chat

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
    val conversationId: String,
    val fromUser: Boolean,
    val imagePath: String?,
    val chat: String,
    val timeStamp: Long = Clock.System.now().toEpochMilliseconds()
)

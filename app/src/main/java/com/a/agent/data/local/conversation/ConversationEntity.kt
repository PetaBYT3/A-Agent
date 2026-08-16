@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.data.local.conversation

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "conversationEntity"
)
data class ConversationEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val title: String
) {
    companion object {
        val Empty = ConversationEntity(
            id = "",
            title = ""
        )
    }
}
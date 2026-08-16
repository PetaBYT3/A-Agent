package com.a.agent.data.local.chat

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Upsert
    suspend fun upsertChat(chatEntity: ChatEntity)

    @Query("SELECT * FROM chatEntity WHERE conversationId = :conversationId ORDER BY timeStamp DESC")
    fun getChats(conversationId: String): Flow<List<ChatEntity>>
}
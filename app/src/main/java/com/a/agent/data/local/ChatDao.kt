package com.a.agent.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Upsert
    suspend fun upsertChat(chatEntity: ChatEntity)

    @Query("SELECT * FROM chatEntity WHERE conversationId = :conversationId ORDER BY timeStamp ASC")
    fun getChats(conversationId: String): Flow<List<ChatEntity>>

    @Query("DELETE FROM chatEntity WHERE conversationId = :conversationId")
    suspend fun clearChat(conversationId: String)
}
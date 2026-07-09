package com.a.agent.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Upsert
    suspend fun upsertChat(chatEntity: ChatEntity)

    @Query("SELECT * FROM chatEntity WHERE modelId = :modelId ORDER BY timeStamp ASC")
    fun getChats(modelId: String): Flow<List<ChatEntity>>

    @Query("DELETE FROM chatEntity WHERE modelId = :modelId")
    suspend fun clearChat(modelId: String)
}
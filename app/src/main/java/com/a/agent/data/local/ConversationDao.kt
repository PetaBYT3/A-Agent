package com.a.agent.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Upsert
    suspend fun upsertConversation(conversationEntity: ConversationEntity)

    @Query("SELECT * FROM conversationEntity")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversationEntity WHERE id = :id")
    fun getConversation(id: String): Flow<ConversationEntity?>

    @Query("DELETE FROM conversationEntity WHERE id = :id")
    fun deleteConversation(id: String)
}
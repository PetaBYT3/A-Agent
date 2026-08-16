package com.a.agent.data.local.conversation

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

    @Query("""
        SELECT conversationEntity.*, COUNT(chatEntity.id) AS totalChats 
        FROM conversationEntity 
        LEFT JOIN chatEntity ON conversationEntity.id = chatEntity.conversationId 
        GROUP BY conversationEntity.id
    """)
    fun getConversationDetails(): Flow<List<ConversationDetailEntity>>

    @Query("SELECT * FROM conversationEntity WHERE id = :id")
    fun getConversation(id: String): Flow<ConversationEntity?>

    @Query("DELETE FROM conversationEntity WHERE id = :id")
    fun deleteConversation(id: String)

    @Query("DELETE FROM chatEntity WHERE conversationId = :conversationId")
    suspend fun clearChat(conversationId: String)
}
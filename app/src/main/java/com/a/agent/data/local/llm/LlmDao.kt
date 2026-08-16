package com.a.agent.data.local.llm

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LlmDao {
    @Query("SELECT * FROM llmEntity")
    fun getLlms(): Flow<List<LlmEntity>>

    @Query("SELECT * FROM llmEntity WHERE id = :id")
    fun getLlm(id: String): Flow<LlmEntity?>

    @Upsert
    suspend fun upsertLlm(llmEntity: LlmEntity)

    @Query("DELETE FROM llmEntity WHERE id = :id")
    suspend fun deleteLlm(id: String)
}
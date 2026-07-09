package com.a.agent.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    @Query("SELECT * FROM modelEntity")
    fun getModels(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM modelEntity WHERE id = :id")
    fun getModel(id: String): Flow<ModelEntity?>

    @Upsert
    suspend fun upsertModel(modelEntity: ModelEntity)

    @Query("DELETE FROM modelEntity WHERE id = :id")
    suspend fun deleteModel(id: String)
}
package com.a.agent.data.local

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "agentDataStore")

class AgentDataStore(
    private val application: Application
) {
    companion object {
        val SelectedModelId = stringPreferencesKey("selectedModelId")
    }

    val selectedModelId: Flow<String?> = application.dataStore.data.map {
        it[SelectedModelId]
    }

    suspend fun setSelectedModelId(modelId: String) {
        application.dataStore.edit {
            it[SelectedModelId] = modelId
        }
    }
}
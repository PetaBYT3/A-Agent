package com.a.agent.data.local

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a.agent.domain.model.LlmModelEngineBackend
import com.a.agent.domain.model.LlmModelEngineConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "agentDataStore")

class AgentDataStore(
    private val application: Application
) {
    companion object {
        val SelectedModelId = stringPreferencesKey("selectedModelId")
        val ProcessingBackend = stringPreferencesKey("processingBackend")
        val VisionBackend = stringPreferencesKey("visionBackend")
    }

    val llmModelEngineConfiguration: Flow<LlmModelEngineConfiguration> = application.dataStore.data.map {
        val selectedModelId = it[SelectedModelId]
        val processingBackend = it[ProcessingBackend]
        val visionBackend = it[VisionBackend]

        LlmModelEngineConfiguration(
            selectedModelId = selectedModelId ?: "",
            processingBackend = LlmModelEngineBackend.valueOf(processingBackend ?: "GPU"),
            visionBackend = LlmModelEngineBackend.valueOf(visionBackend ?: "GPU")
        )
    }

    suspend fun setLlmModelEngineConfiguration(llmModelEngineConfiguration: LlmModelEngineConfiguration) {
        application.dataStore.edit {
            it[SelectedModelId] = llmModelEngineConfiguration.selectedModelId
            it[ProcessingBackend] = llmModelEngineConfiguration.processingBackend.name
            it[VisionBackend] = llmModelEngineConfiguration.visionBackend.name
        }
    }
}
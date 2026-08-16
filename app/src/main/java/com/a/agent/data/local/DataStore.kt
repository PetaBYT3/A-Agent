package com.a.agent.data.local

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a.agent.domain.model.Configuration
import com.a.agent.domain.model.LlmBackend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "agentDataStore")

class DataStore(
    private val application: Application
) {
    companion object {
        val SelectedModelId = stringPreferencesKey("selectedModelId")
        val ProcessingBackend = stringPreferencesKey("processingBackend")
        val VisionBackend = stringPreferencesKey("visionBackend")
        val MaxNumTokens = intPreferencesKey("maxNumTokens")

        val TTSLanguage = stringPreferencesKey("ttsLanguage")
        val STTLanguage = stringPreferencesKey("sttLanguage")
    }

    val configuration: Flow<Configuration> = application.dataStore.data.map {
        val selectedModelId = it[SelectedModelId]
        val processingBackend = it[ProcessingBackend]
        val visionBackend = it[VisionBackend]
        val maxNumTokens = it[MaxNumTokens]

        Configuration(
            selectedLlmId = selectedModelId ?: "",
            processing = LlmBackend.valueOf(processingBackend ?: LlmBackend.GPU.name),
            vision = LlmBackend.valueOf(visionBackend ?: LlmBackend.GPU.name),
            maxNumTokens = maxNumTokens ?: 128
        )
    }

    suspend fun setLlmModelEngineConfiguration(configuration: Configuration) {
        application.dataStore.edit {
            it[SelectedModelId] = configuration.selectedLlmId
            it[ProcessingBackend] = configuration.processing.name
            it[VisionBackend] = configuration.vision.name
            it[MaxNumTokens] = configuration.maxNumTokens
        }
    }

    val ttsLanguage: Flow<Locale> = application.dataStore.data.map {
        Locale.forLanguageTag(it[TTSLanguage] ?: "en-US")
    }

    suspend fun setTtsLanguage(locale: Locale) {
        application.dataStore.edit {
            it[TTSLanguage] = locale.toLanguageTag()
        }
    }

    val sttLanguage: Flow<Locale> = application.dataStore.data.map {
        Locale.forLanguageTag(it[STTLanguage] ?: "en-US")
    }

    suspend fun setSttLanguage(locale: Locale) {
        application.dataStore.edit {
            it[STTLanguage] = locale.toLanguageTag()
        }
    }
}
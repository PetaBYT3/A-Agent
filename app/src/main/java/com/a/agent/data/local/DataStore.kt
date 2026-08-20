package com.a.agent.data.local

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
        val IsAutomatic = booleanPreferencesKey("IsAutomatic")
        val ProcessingBackend = stringPreferencesKey("processingBackend")
        val VisionBackend = stringPreferencesKey("visionBackend")

        val EnableDefaultKey = booleanPreferencesKey("enableDefaultKey")
        val AuthorizationKey = stringPreferencesKey("authorizationKey")

        val TTSLanguage = stringPreferencesKey("ttsLanguage")
        val STTLanguage = stringPreferencesKey("sttLanguage")
    }

    val configuration: Flow<Configuration> = application.dataStore.data.map {
        val selectedModelId = it[SelectedModelId]
        val isAutomatic = it[IsAutomatic]
        val processingBackend = it[ProcessingBackend]
        val visionBackend = it[VisionBackend]

        Configuration(
            selectedLlmId = selectedModelId ?: "",
            isAutomatic = isAutomatic ?: true,
            processing = LlmBackend.valueOf(processingBackend ?: LlmBackend.CPU.name),
            vision = LlmBackend.valueOf(visionBackend ?: LlmBackend.CPU.name)
        )
    }

    suspend fun setLlmModelEngineConfiguration(configuration: Configuration) {
        application.dataStore.edit {
            it[SelectedModelId] = configuration.selectedLlmId
            it[IsAutomatic] = configuration.isAutomatic
            it[ProcessingBackend] = configuration.processing.name
            it[VisionBackend] = configuration.vision.name
        }
    }

    val enableDefaultKey: Flow<Boolean> = application.dataStore.data.map {
        it[EnableDefaultKey] ?: true
    }

    suspend fun setEnableDefaultKey(enabled: Boolean) {
        application.dataStore.edit {
            it[EnableDefaultKey] = enabled
        }
    }

    val authorizationKey: Flow<String> = application.dataStore.data.map {
        it[AuthorizationKey] ?: ""
    }

    suspend fun setAuthorizationKey(key: String) {
        application.dataStore.edit {
            it[AuthorizationKey] = key
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
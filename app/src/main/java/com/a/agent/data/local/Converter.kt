package com.a.agent.data.local

import androidx.room.TypeConverter
import com.a.agent.data.local.llm.LlmSource

class Converter {
    @TypeConverter
    fun fromLlmSource(llmSource: LlmSource): String = llmSource.name

    @TypeConverter
    fun toLlmSource(modelSource: String): LlmSource = LlmSource.valueOf(modelSource)
}
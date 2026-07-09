package com.a.agent.data.local

import androidx.room.TypeConverter
import java.io.File

class Converter {
    @TypeConverter
    fun fromFile(file: File): String = file.absolutePath

    @TypeConverter
    fun toFile(path: String): File = File(path)

    @TypeConverter
    fun fromModelType(modelType: ModelType): String = modelType.name

    @TypeConverter
    fun toModelType(name: String): ModelType = ModelType.valueOf(name)
}
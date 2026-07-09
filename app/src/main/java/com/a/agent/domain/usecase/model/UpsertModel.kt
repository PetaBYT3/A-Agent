@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.domain.usecase.model

import android.app.Application
import arrow.core.Either
import arrow.core.left
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelType
import com.a.agent.domain.repository.ModelRepository
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

class UpsertModel(
    private val application: Application,
    private val modelRepository: ModelRepository,
) {
    suspend operator fun invoke(
        id: String,
        url: String,
        name: String,
        type: ModelType?,
        filename: String,
        totalBytes: Long,
        isSupported: Boolean
    ): Either<String, Unit> {
        when {
            url.isBlank() -> return "Empty Url".left()
            name.isBlank() -> return "Empty Name".left()
            type == null -> return "Type Not Selected".left()
            filename.isBlank() || totalBytes == 0L -> return "No Metadata".left()
        }

        val modelEntity = ModelEntity(
            id = id,
            type = type,
            name = name,
            url = url,
            path = File(application.getExternalFilesDir(null), type.name + File.separator + filename),
            fileName = filename,
            totalBytes = totalBytes,
            isSupported = isSupported
        )

        return modelRepository.upsertModel(modelEntity)
    }
}
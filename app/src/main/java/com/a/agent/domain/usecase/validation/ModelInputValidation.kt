@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.domain.usecase.validation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.a.agent.data.local.ModelType
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ModelInputValidation {
    operator fun invoke(
        id: String,
        url: String,
        path: File,
        name: String,
        type: ModelType?,
        filename: String?,
        totalBytes: Long?,
        isSupported: Boolean?
    ): Either<String, Unit> {
        if (url.isBlank()) {
            return "Fill Url".left()
        }

        if (name.isBlank()) {
            return "Fill Name".left()
        }

        if (type == null) {
            return "Choose Type".left()
        }

        if (filename.isNullOrBlank() || totalBytes == null || isSupported == null) {
            return "No Metadata".left()
        }

        if (!isSupported) {
            return "Model Not Supported".left()
        }

        return Unit.right()
    }
}
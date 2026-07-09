package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.remote.ModelMetadataDto
import com.a.agent.domain.repository.ModelRepository

class GetModelMetadata(
    private val modelRepository: ModelRepository
) {
    suspend operator fun invoke(url: String): Either<String, ModelMetadataDto> {
        return modelRepository.getModelMetadata(url)
    }
}
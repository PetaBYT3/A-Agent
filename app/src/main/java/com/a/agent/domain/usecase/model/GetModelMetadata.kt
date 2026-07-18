package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.remote.ModelMetadataDto
import com.a.agent.domain.repository.LlmModelManagerRepository
import kotlinx.coroutines.flow.Flow

class GetModelMetadata(
    private val llmModelManagerRepository: LlmModelManagerRepository
) {
    suspend operator fun invoke(url: String): Flow<Either<String, ModelMetadataDto>> {
        return llmModelManagerRepository.getModelMetadata(url)
    }
}
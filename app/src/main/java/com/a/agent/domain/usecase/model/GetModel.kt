package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.LlmModelManagerRepository
import kotlinx.coroutines.flow.Flow

class GetModel(
    private val llmModelManagerRepository: LlmModelManagerRepository
) {
    suspend operator fun invoke(modelId: String): Flow<Either<String, ModelEntity>> {
        return llmModelManagerRepository.getModel(modelId)
    }
}
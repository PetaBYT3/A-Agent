package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.LlmModelManagerRepository
import kotlinx.coroutines.flow.Flow

class GetModels(
    private val llmModelManagerRepository: LlmModelManagerRepository
) {
    suspend operator fun invoke(): Flow<Either<String, List<ModelEntity>>> {
        return llmModelManagerRepository.getModels()
    }
}
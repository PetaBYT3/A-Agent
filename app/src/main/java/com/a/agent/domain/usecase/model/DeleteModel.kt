package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.LlmModelManagerRepository
import kotlinx.coroutines.flow.Flow

class DeleteModel(
    private val llmModelManagerRepository: LlmModelManagerRepository
) {
    suspend operator fun invoke(modelEntity: ModelEntity): Flow<Either<String, Unit>> {
        return llmModelManagerRepository.deleteModel(modelEntity)
    }
}
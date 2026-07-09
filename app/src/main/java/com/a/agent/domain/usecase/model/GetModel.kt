package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.ModelRepository
import kotlinx.coroutines.flow.Flow

class GetModel(
    private val modelRepository: ModelRepository
) {
    suspend operator fun invoke(modelId: String): Flow<Either<String, ModelEntity>> {
        return modelRepository.getModel(modelId)
    }
}
package com.a.agent.domain.usecase.model

import arrow.core.Either
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.ModelRepository

class ToggleDownload(
    private val modelRepository: ModelRepository
) {
    suspend operator fun invoke(modelEntity: ModelEntity): Either<String, Unit> {
        return modelRepository.toggleDownload(modelEntity)
    }
}
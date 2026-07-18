@file:OptIn(ExperimentalUuidApi::class)

package com.a.agent.domain.usecase.model

import android.app.Application
import arrow.core.Either
import arrow.core.left
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelType
import com.a.agent.domain.repository.LlmModelManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

class UpsertModel(
    private val application: Application,
    private val llmModelManagerRepository: LlmModelManagerRepository,
) {
    suspend operator fun invoke(
        id: String,
        url: String,
        name: String,
        filename: String,
        totalBytes: Long,
        isSupported: Boolean
    ): Flow<Either<String, Unit>> = llmModelManagerRepository.upsertModel(ModelEntity.Empty)
}
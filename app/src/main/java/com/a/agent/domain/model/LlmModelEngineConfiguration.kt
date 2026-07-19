package com.a.agent.domain.model

data class LlmModelEngineConfiguration(
    val selectedModelId: String,
    val processingBackend: LlmModelEngineBackend,
    val visionBackend: LlmModelEngineBackend
)

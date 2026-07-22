package com.a.agent.domain.model

data class LlmModelEngineConfiguration(
    val selectedModelId: String,
    val processingBackend: LlmModelEngineBackend,
    val visionBackend: LlmModelEngineBackend,
    val maxNumTokens: Int
) {
    companion object {
        val Empty = LlmModelEngineConfiguration(
            selectedModelId = "",
            processingBackend = LlmModelEngineBackend.GPU,
            visionBackend = LlmModelEngineBackend.GPU,
            maxNumTokens = 0
        )
    }
}

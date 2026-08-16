package com.a.agent.domain.model

data class Configuration(
    val selectedLlmId: String,
    val processing: LlmBackend,
    val vision: LlmBackend,
    val maxNumTokens: Int
) {
    companion object {
        val Empty = Configuration(
            selectedLlmId = "",
            processing = LlmBackend.GPU,
            vision = LlmBackend.GPU,
            maxNumTokens = 0
        )
    }
}

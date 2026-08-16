package com.a.agent.domain.model

enum class Directory(
    val absolutePath: String
) {
    Llms("llms"),
    TextToSpeech("tts"),
    SpeechToText("stt"),
    Image("images")
}
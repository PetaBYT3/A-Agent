package com.a.agent.data.mapper

fun Exception.toMessage(): String {
    return this.message ?: "Unknown Error"
}

fun Throwable.toMessage(): String {
    return this.message ?: "Unknown Error"
}
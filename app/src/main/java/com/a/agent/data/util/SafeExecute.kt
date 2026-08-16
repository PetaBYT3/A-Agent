package com.a.agent.data.util

import arrow.core.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

fun <T> safeExecute(
    block: suspend FlowCollector<T>.() -> Unit
): Flow<Either<String, T>> = flow {
    try {
        val proxyCollector = FlowCollector<T> { value ->
            emit(Either.Right(value))
        }
        proxyCollector.block()
    } catch (e: Exception) {
        emit(Either.Left(e.message ?: "Unknown error"))
    }
}
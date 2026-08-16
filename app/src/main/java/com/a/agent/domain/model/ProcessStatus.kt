package com.a.agent.domain.model

sealed interface ProcessStatus<out T> {
    data class OnProcess<T>(val process: T): ProcessStatus<T>
    data object OnCompletion: ProcessStatus<Nothing>
}
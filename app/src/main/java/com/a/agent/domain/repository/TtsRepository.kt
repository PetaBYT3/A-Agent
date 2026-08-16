package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.domain.model.ProcessStatus
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface TtsRepository {
    val isTtsOnline: Flow<Boolean>

    val selectedLanguage: Flow<Locale>
    fun setLanguage(locale: Locale): Flow<Either<String, Unit>>
    fun initialize(): Flow<Either<String, ProcessStatus<Set<Locale>>>>
    fun start(text: String): Flow<Either<String, ProcessStatus<Unit>>>
    fun stop()
    fun destroy()
}
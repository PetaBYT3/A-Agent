package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.domain.model.ProcessStatus
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface SttRepository {

    val selectedLanguage: Flow<Locale>
    fun getSstLanguages(): Flow<List<Locale>>
    fun setTtsLanguage(locale: Locale): Flow<Either<String, Unit>>
    fun start(): Flow<Either<String, ProcessStatus<String>>>
    fun stop()
    fun destroy()
}
package com.a.agent.domain.repository

import arrow.core.Either
import com.a.agent.domain.model.ProcessStatus
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

interface BackupRepository {
    fun exportBackup(platformFile: PlatformFile): Flow<Either<String, ProcessStatus<Pair<String, Float>>>>
    fun importBackup(platformFile: PlatformFile): Flow<Either<String, ProcessStatus<Pair<String, Float>>>>
}
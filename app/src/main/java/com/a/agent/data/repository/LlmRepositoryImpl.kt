package com.a.agent.data.repository

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import arrow.core.Either
import com.a.agent.data.local.Database
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.data.mapper.toMessage
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.data.remote.LlmApi
import com.a.agent.data.util.toMegaByte
import com.a.agent.domain.model.Directory
import com.a.agent.domain.model.LlmMetadata
import com.a.agent.domain.repository.DirectoryRepository
import com.a.agent.domain.repository.LlmRepository
import com.a.agent.service.DownloadService
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class LlmRepositoryImpl(
    private val application: Application,
    private val llmApi: LlmApi,
    private val database: Database,
    private val directoryRepository: DirectoryRepository
): LlmRepository {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkAndUpsertInitialData()
        }
    }

    private suspend fun checkAndUpsertInitialData() {
        val llm = database.llmDao.getLlms().first()
        val initialLlm = listOf(
            LlmEntity(
                name = "Gemma 4 E2B",
                url = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
                path = directoryRepository.setDirectory(Directory.Llms, "gemma-4-E2B-it.litertlm").absolutePath,
                fileName = "gemma-4-E2B-it.litertlm",
                totalBytes = 2588147712,
                llmSource = LlmSource.Default,
                isDownloaded = false
            ),
            LlmEntity(
                name = "Qwen 2.5 1.5B",
                url = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
                path = directoryRepository.setDirectory(Directory.Llms, "qwen-2.5-1.5b.litertlm").absolutePath,
                fileName = "qwen-2.5-1.5b.litertlm",
                totalBytes = 2597931520,
                llmSource = LlmSource.Default,
                isDownloaded = false
            ),
        )

        if (llm.isEmpty()) {
            initialLlm.forEach { modelEntity ->
                database.llmDao.upsertLlm(modelEntity)
            }
        }
    }

    override fun getMetadataFromUrl(url: String): Flow<Either<String, LlmMetadata>> {
        return flow<Either<String, LlmMetadata>> {
            val llmMetadataDto = llmApi.getLlmMetadata(url)
            val targetFile = directoryRepository.setDirectory(Directory.Llms, llmMetadataDto.fileName)
            val llmMetadata = LlmMetadata(
                fileName = llmMetadataDto.fileName,
                filePath = targetFile.absolutePath,
                totalBytes = llmMetadataDto.totalBytes,
                isSupported = llmMetadataDto.isSupported
            )
            emit(Either.Right(llmMetadata))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getMetadataFromLocal(platformFile: PlatformFile): Flow<Either<String, LlmMetadata>> = flow {
        try {
            val targetFile = directoryRepository.setDirectory(Directory.Llms, platformFile.name)
            val llmMetadata = LlmMetadata(
                fileName = platformFile.name,
                filePath = targetFile.absolutePath,
                totalBytes = platformFile.size(),
                isSupported = platformFile.name.endsWith(".litertlm", ignoreCase = true)
            )
            emit(Either.Right(llmMetadata))
        } catch (e: Exception) {
            emit(Either.Left(e.toMessage()))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLlm(id: String): Flow<Either<String, LlmEntity?>> = flow {
        try {
            database.llmDao.getLlm(id).collect { llmEntity ->
                emit(Either.Right(llmEntity))
            }
        } catch (e: Exception) {
            emit(Either.Left(e.toMessage()))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLlms(llmSource: LlmSource?): Flow<Either<String, List<LlmEntity>>> = flow {
        try {
            database.llmDao.getLlms().collect { modelEntities ->
                val validatedLlmEntities = modelEntities.map { llmEntity ->
                    val expectedSize = llmEntity.totalBytes.toMegaByte()
                    val modelSize = File(llmEntity.path).length().toMegaByte()

                    llmEntity.copy(
                        isDownloaded = expectedSize == modelSize
                    )
                }
                when (llmSource) {
                    LlmSource.Default -> {
                        val filteredLlm = validatedLlmEntities.filter { llmEntity ->
                            llmEntity.llmSource == LlmSource.Default
                        }
                        emit(Either.Right(filteredLlm))
                    }
                    LlmSource.Url -> {
                        val filteredLlm = validatedLlmEntities.filter { llmEntity ->
                            llmEntity.llmSource == LlmSource.Url
                        }
                        emit(Either.Right(filteredLlm))
                    }
                    LlmSource.Local -> {
                        val filteredLlm = validatedLlmEntities.filter { llmEntity ->
                            llmEntity.llmSource == LlmSource.Local
                        }
                        emit(Either.Right(filteredLlm))
                    }
                    null -> {
                        emit(Either.Right(validatedLlmEntities))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Either.Left(e.toMessage()))
        }
    }.flowOn(Dispatchers.IO)

    override fun upsertLlm(llmEntity: LlmEntity): Flow<Either<String, String>> = flow {
        try {
            database.llmDao.upsertLlm(llmEntity)
            emit(Either.Right("Llm Upserted"))
        } catch (e: Exception) {
            emit(Either.Left(e.toMessage()))
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteLlm(llmEntity: LlmEntity): Flow<Either<String, String>> = flow {
        try {
            database.llmDao.deleteLlm(llmEntity.id)
            val targetFile = File(llmEntity.path)
            if (targetFile.exists()) targetFile.delete()
            emit(Either.Right("Llm Deleted"))
        } catch (e: Exception) {
            emit(Either.Left(e.toMessage()))
        }
    }.flowOn(Dispatchers.IO)

    override fun copyFromLocal(platformFile: PlatformFile): Flow<Either<String, Unit>> {
        return flow<Either<String, Unit>> {
            val targetFile = directoryRepository.setDirectory(Directory.Llms, platformFile.name)
            application.contentResolver.openInputStream(platformFile.toAndroidUri()).use { inputStream ->
                targetFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                    outputStream.flush()
                }
            }
            emit(Either.Right(Unit))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    private val activeDownloadJob = ConcurrentHashMap<String, Job>()

    private val _activeDownloadMap = MutableStateFlow<Map<String, Pair<String, DownloadInfo>>>(emptyMap())
    override val activeDownloadMap = _activeDownloadMap.asStateFlow()

    override fun toggleDownload(llmEntity: LlmEntity): Flow<Either<String, String>> {
        return flow<Either<String, String>> {
            val serviceIntent = Intent(application, DownloadService::class.java)
            val requestedJob = activeDownloadJob[llmEntity.id]

            if (requestedJob != null && requestedJob.isActive) {
                stopDownload(llmEntity.id)
                stopService(serviceIntent)
                emit(Either.Right("Download Stopped"))
            } else {
                startService(serviceIntent)
                activeDownloadJob[llmEntity.id] = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    _activeDownloadMap.update { it + (llmEntity.id to Pair("", DownloadInfo(0L, 0L, 0f, 0))) }
                    llmApi.getLlmFile(llmEntity.url, llmEntity.path).collect { downloadInfo ->
                        _activeDownloadMap.update { it + (llmEntity.id to Pair(llmEntity.fileName, downloadInfo)) }
                    }
                    database.llmDao.upsertLlm(llmEntity.copy(isDownloaded = true))
                    _activeDownloadMap.update { it - llmEntity.id }
                    stopService(serviceIntent)
                }
                emit(Either.Right("Download Started"))
            }
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    private fun stopDownload(id: String) {
        activeDownloadJob.remove(id)?.cancel()
        _activeDownloadMap.update { it - id }
    }

    private fun stopService(intent: Intent) {
        if (activeDownloadJob.isEmpty()) application.stopService(intent)
    }

    private fun startService(intent: Intent) {
        ContextCompat.startForegroundService(application, intent)
    }

    override fun deleteOrphanFile(): Flow<Either<String, String>> {
        TODO("Not yet implemented")
    }
}
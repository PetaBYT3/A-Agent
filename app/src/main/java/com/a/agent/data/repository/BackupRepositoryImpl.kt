@file:Suppress("BlockingMethodInNonBlockingContext")

package com.a.agent.data.repository

import android.app.Application
import arrow.core.Either
import com.a.agent.BuildConfig
import com.a.agent.data.local.Database
import com.a.agent.data.mapper.toMessage
import com.a.agent.data.util.jsonByteArrayToDataClass
import com.a.agent.domain.model.BackupMetadata
import com.a.agent.domain.model.Directory
import com.a.agent.domain.model.ProcessStatus
import com.a.agent.domain.repository.BackupRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.time.Clock

class BackupRepositoryImpl(
    private val application: Application,
    private val database: Database
): BackupRepository {
    private fun zipMetadataFile(
        zipOutputStream: ZipOutputStream,
        totalFiles: Int
    ) {
        val backupMetadata = BackupMetadata(
            identifier = BuildConfig.APPLICATION_ID,
            totalFiles = totalFiles,
            created = Clock.System.now().toEpochMilliseconds()
        )
        val backupMetadataEntry = ZipEntry("metadata.json")
        val backupMetadataJson = Json.encodeToString(backupMetadata)

        zipOutputStream.putNextEntry(backupMetadataEntry)
        zipOutputStream.write(backupMetadataJson.toByteArray(Charsets.UTF_8))
        zipOutputStream.closeEntry()
    }

    private suspend fun zipFileOrFolder(
        fileToZip: File,
        entryName: String,
        zipOutputStream: ZipOutputStream,
        onProgress: suspend (fileName: String) -> Unit
    ) {
        if (fileToZip.isDirectory) {
            val files = fileToZip.listFiles()

            if (files.isNullOrEmpty()) {
                val zipEntry = ZipEntry(if (entryName.endsWith("/")) entryName else "$entryName/")
                zipOutputStream.putNextEntry(zipEntry)
                zipOutputStream.closeEntry()
                return
            }

            files.forEach { file ->
                zipFileOrFolder(file, "$entryName/${file.name}", zipOutputStream, onProgress)
            }
        } else {
            FileInputStream(fileToZip).use { fileInputStream ->
                val zipEntry = ZipEntry(entryName)
                zipOutputStream.putNextEntry(zipEntry)

                val bytes = ByteArray(1024)
                var length: Int
                while (fileInputStream.read(bytes).also { length = it } >= 0) {
                    zipOutputStream.write(bytes, 0, length)
                }
            }
            onProgress(fileToZip.name)
        }
    }

    override fun exportBackup(platformFile: PlatformFile): Flow<Either<String, ProcessStatus<Pair<String, Float>>>> {
        return flow {
            val targetOutputStream = application.contentResolver.openOutputStream(platformFile.toAndroidUri())
            if (targetOutputStream == null) {
                emit(Either.Left("Fail To Open Directory Path"))
            }

            val databaseFile = application.getDatabasePath(BuildConfig.DATABASE_NAME)
            val walFile = File(databaseFile.path + "-wal")
            val shmFile = File(databaseFile.path + "-shm")
            val llmFolder = File(application.getExternalFilesDir(null), Directory.Llms.absolutePath)
            val imageFolder = File(application.getExternalFilesDir(null), Directory.Image.absolutePath)

            val filesToZip = listOf(
                databaseFile,
                walFile,
                shmFile,
                llmFolder,
                imageFolder
            ).filter { it.exists() }
            val totalFilesCount = filesToZip.sumOf { file ->
                file.walkTopDown().count { it.isFile }
            }

            var currentZippedFiles = 0
            ZipOutputStream(targetOutputStream).use { zipOutputStream ->
                emit(Either.Right(ProcessStatus.OnProcess(Pair("Metadata", 0f))))
                zipMetadataFile(zipOutputStream, totalFilesCount)

                filesToZip.forEach { file ->
                    zipFileOrFolder(
                        fileToZip = file,
                        entryName = file.name,
                        zipOutputStream = zipOutputStream,
                        onProgress = { fileName ->
                            currentZippedFiles++

                            val progressFloat = currentZippedFiles / totalFilesCount.toFloat()
                            emit(Either.Right(ProcessStatus.OnProcess(Pair(fileName, progressFloat))))
                        }
                    )
                }
            }
            emit(Either.Right(ProcessStatus.OnCompletion))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun validateBackupZip(
        zipInputStream: ZipInputStream,
        onResult: suspend (isBackupValid: Boolean, totalFilesInZip: Int) -> Unit
    ) {
        var isBackupZipValid = false
        var totalFilesInZip = 0
        var zipEntry = zipInputStream.nextEntry

        while (zipEntry != null) {
            val entryName = zipEntry.name

            if (entryName == BuildConfig.BACKUP_METADATA_FILENAME) {
                val metadata = jsonByteArrayToDataClass<BackupMetadata>(zipInputStream.readBytes())
                if (metadata != null) {
                    isBackupZipValid = true
                    totalFilesInZip = metadata.totalFiles
                }
                break
            }

            zipInputStream.closeEntry()
            zipEntry = zipInputStream.nextEntry
        }

        onResult(isBackupZipValid, totalFilesInZip)
    }

    private suspend fun copyBackupZip(
        zipInputStream: ZipInputStream,
        onResult: suspend (fileNameInZip: String, currentFileCopied: Int) -> Unit
    ) {
        val targetDatabaseFile = listOf(
            BuildConfig.DATABASE_NAME,
            "${BuildConfig.DATABASE_NAME}-shm",
            "${BuildConfig.DATABASE_NAME}-wal"
        )
        val targetAppDataFolder = listOf(
            "images/",
            "llms/"
        )

        database.close()
        val databaseDirectory = application.getDatabasePath(BuildConfig.DATABASE_NAME).parentFile
        val appDataDirectory = application.getExternalFilesDir(null)

        var currentCopiedFiles = 0
        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val entryName = zipEntry.name
            val fileNameInZip = File(entryName).name

            val isDatabaseFile = fileNameInZip in targetDatabaseFile
            val isAppDataFile = targetAppDataFolder.any { entryName.startsWith(it) }
            if (!zipEntry.isDirectory && (isDatabaseFile || isAppDataFile)) {
                currentCopiedFiles++

                val targetLocalFile = if (isDatabaseFile) {
                    File(databaseDirectory, fileNameInZip)
                } else {
                    File(appDataDirectory, entryName)
                }
                FileOutputStream(targetLocalFile).use { fileOutputStream ->
                    zipInputStream.copyTo(fileOutputStream)
                }
                onResult(fileNameInZip, currentCopiedFiles)
            }

            zipInputStream.closeEntry()
            zipEntry = zipInputStream.nextEntry
        }
    }

    override fun importBackup(platformFile: PlatformFile): Flow<Either<String, ProcessStatus<Pair<String, Float>>>> {
        return flow {
            val validateStream = application.contentResolver.openInputStream(platformFile.toAndroidUri())
            if (validateStream == null) {
                emit(Either.Left("Cannot Read File"))
                return@flow
            }

            var isBackupFileValidated = false
            var countedTotalFilesInZip = 0
            validateStream.use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    validateBackupZip(
                        zipInputStream = zipInputStream,
                        onResult = { isBackupValid, totalFilesInZip ->
                            isBackupFileValidated = isBackupValid
                            countedTotalFilesInZip = totalFilesInZip
                        }
                    )
                }
            }

            if (!isBackupFileValidated) {
                emit(Either.Left("Backup File Invalid"))
                return@flow
            }

            val copyStream = application.contentResolver.openInputStream(platformFile.toAndroidUri())
            if (copyStream == null) {
                emit(Either.Left("Cannot Open File For Copying"))
                return@flow
            }

            copyStream.use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    copyBackupZip(
                        zipInputStream = zipInputStream,
                        onResult = { fileNameInZip, currentFileCopied ->
                            val progressFloat = currentFileCopied.toFloat() / countedTotalFilesInZip
                            emit(Either.Right(ProcessStatus.OnProcess(Pair(fileNameInZip, progressFloat))))
                        }
                    )
                }
            }
            emit(Either.Right(ProcessStatus.OnCompletion))
        }.catch { throwable ->
            emit(Either.Left(throwable.toMessage()))
        }.flowOn(Dispatchers.IO)
    }
}
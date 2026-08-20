package com.a.agent.data.remote

import com.a.agent.BuildConfig
import com.a.agent.data.local.DataStore
import com.a.agent.data.util.UnavailableDataException
import com.a.agent.util.VerEx
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class LlmApiImpl(
    private val httpClient: HttpClient,
    private val dataStore: DataStore
): LlmApi {
    private suspend fun huggingFaceToken(): String  {
        return if (dataStore.enableDefaultKey.first()) {
            dataStore.authorizationKey.first()
        } else {
            BuildConfig.HUGGINGFACE_TOKEN
        }
    }

    private val fileNameRegex = VerEx()
        .add("filename=")
        .add("\"?")
        .anythingBut("\"")
        .add("\"?")
        .toRegex()

    override suspend fun getLlmMetadata(url: String): LlmMetadataDto {
        val response = httpClient.head(url) {
            header("Authorization", "Bearer ${huggingFaceToken()}")
        }

        val fileName = response.headers[HttpHeaders.ContentDisposition]?.let {
            fileNameRegex.find(it)?.groupValues?.get(1)
        }
        val totalBytes = response.contentLength()

        if (fileName == null && totalBytes == null) throw UnavailableDataException("Invalid Url")

        val isSupported = fileName?.endsWith(".litertlm", ignoreCase = true) ?: false

        return LlmMetadataDto(
            fileName = fileName!!,
            totalBytes = totalBytes!!,
            isSupported = isSupported
        )
    }

    override suspend fun getLlmFile(url: String, path: String): Flow<DownloadInfo> {
        return flow {
            httpClient.prepareGet(url) {
                header("Authorization", "Bearer ${huggingFaceToken()}")
                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                }
            }.execute { response ->
                val channel = response.bodyAsChannel()

                val totalBytes = response.contentLength() ?: 0L
                var downloadedBytes = 0L

                val buffer = ByteArray(8 * 1024)

                File(path).outputStream().use { outputStream ->
                    while (!channel.isClosedForRead) {
                        val byteRead = channel.readAvailable(buffer)
                        if (byteRead != -1) {
                            outputStream.write(buffer, 0, byteRead)
                            downloadedBytes += byteRead

                            val progressFloat = if (totalBytes > 0) {
                                downloadedBytes.toFloat() / totalBytes.toFloat()
                            } else 0f
                            val progressPercentage = if (totalBytes > 0) {
                                ((downloadedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
                            } else 0

                            val downloadProgress = DownloadInfo(
                                totalBytes = totalBytes,
                                downloadedBytes = downloadedBytes,
                                progress = progressFloat,
                                percentage = progressPercentage
                            )
                            emit(downloadProgress)
                        }
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }
}
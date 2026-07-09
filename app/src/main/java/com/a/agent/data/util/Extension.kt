package com.a.agent.data.util

import com.a.agent.data.remote.DownloadInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

fun simulateDownload(
    totalSizeMB: Long = 150L,
    speedKbps: Long = 2048L
): Flow<DownloadInfo> = flow {

    val totalBytes = totalSizeMB * 1024 * 1024
    var downloadedBytes = 0L

    val chunkSize = (speedKbps * 1024) / 10

    emit(DownloadInfo(totalBytes, 0L, 0f, 0))

    while (downloadedBytes < totalBytes) {
        delay(100.milliseconds)

        downloadedBytes += chunkSize

        if (downloadedBytes > totalBytes) {
            downloadedBytes = totalBytes
        }

        val progress = downloadedBytes.toFloat() / totalBytes.toFloat()
        val percentage = ((downloadedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

        emit(
            DownloadInfo(
                totalBytes = totalBytes,
                downloadedBytes = downloadedBytes,
                progress = progress,
                percentage = percentage
            )
        )
    }
}
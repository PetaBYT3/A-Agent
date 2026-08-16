package com.a.agent.data.util

import com.a.agent.domain.model.ByteProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyToWithProgress(
    outputStream: OutputStream,
    totalBytes: Long,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
): Flow<ByteProgress> {
    return flow {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = read(buffer)
        var lastEmittedPercentage = -1

        val totalMegaByteString = if (totalBytes > 0) {
            totalBytes.toMegaByte()
        } else {
            "Unknown"
        }

        while (bytes >= 0) {
            outputStream.write(buffer, 0, bytes)
            bytesCopied += bytes

            if (totalBytes > 0) {
                val progressFloat = bytesCopied.toFloat() / totalBytes.toFloat()
                val percentageInt = (progressFloat * 100).toInt()

                if (percentageInt != lastEmittedPercentage || bytesCopied == totalBytes) {
                    val byteProgressString = "${bytesCopied.toMegaByte()} / $totalMegaByteString"
                    val byteProgress = ByteProgress(
                        byteProgress = byteProgressString,
                        percentage = percentageInt,
                        progress = progressFloat
                    )
                    emit(byteProgress)
                    lastEmittedPercentage = percentageInt
                }
            }

            bytes = read(buffer)
        }
    }.flowOn(Dispatchers.IO)
}
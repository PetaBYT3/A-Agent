package com.a.agent.presentation.util

import android.content.Context
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.Uuid

fun Long.toMegaByte(): String {
    val size = this / (1024 * 1024)
    return "$size MB"
}

suspend fun PlatformFile.saveToCacheDir(
    context: Context,
    extension: String
): String = withContext(Dispatchers.IO) {
    val fileBytes = this@saveToCacheDir.readBytes()
    val cacheFile = File(context.cacheDir, "${Uuid.random()}.$extension")
    cacheFile.writeBytes(fileBytes)

    cacheFile.absolutePath
}
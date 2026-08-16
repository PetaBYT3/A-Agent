package com.a.agent.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.a.agent.BuildConfig
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess
import kotlin.uuid.Uuid

suspend fun PlatformFile.saveToCacheDir(
    context: Context,
    extension: String
): String = withContext(Dispatchers.IO) {
    val fileBytes = this@saveToCacheDir.readBytes()
    val cacheFile = File(context.cacheDir, "${Uuid.random()}.$extension")
    cacheFile.writeBytes(fileBytes)

    cacheFile.absolutePath
}

fun restartApplication(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    val componentName = intent?.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(mainIntent)
    exitProcess(0)
}

fun openApplicationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    }
    context.startActivity(intent)
}
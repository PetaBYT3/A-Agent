package com.a.agent.data.repository

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import com.a.agent.BuildConfig
import com.a.agent.domain.model.Directory
import com.a.agent.domain.repository.DirectoryRepository
import java.io.File

class DirectoryRepositoryImpl(
    private val application: Application
): DirectoryRepository {
    private val databaseDirectory = application.getDatabasePath(BuildConfig.DATABASE_NAME).parentFile

    private val rootDirectory = application.getExternalFilesDir(null)
    private val llmDirectory = File(rootDirectory, Directory.Llms.absolutePath)
    private val ttsDirectory = File(rootDirectory, Directory.TextToSpeech.absolutePath)
    private val sstDirectory = File(rootDirectory, Directory.SpeechToText.absolutePath)
    private val imageDirectory = File(rootDirectory, Directory.Image.absolutePath)

    override fun initializeDirectory() {
        if (databaseDirectory != null && !databaseDirectory.exists()) databaseDirectory.mkdirs()
        if (!llmDirectory.exists()) llmDirectory.mkdirs()
        if (!ttsDirectory.exists()) ttsDirectory.mkdirs()
        if (!sstDirectory.exists()) sstDirectory.mkdirs()
        if (!imageDirectory.exists()) imageDirectory.mkdirs()
    }

    override fun setDirectory(directory: Directory, fileName: String): File {
        return when (directory) {
            Directory.Llms -> File(llmDirectory, fileName)
            Directory.TextToSpeech -> File(ttsDirectory, fileName)
            Directory.SpeechToText -> File(sstDirectory, fileName)
            Directory.Image -> File(imageDirectory, fileName)
        }
    }

    override fun openFolder(targetPath: String) {
        val clearTargetFolder = targetPath
            .removePrefix("/storage/emulated/0")
            .removePrefix("sdcard/")
            .removePrefix("/")
        val encodedPath = clearTargetFolder.replace("/", "%2F")
        val folderUri = "content://com.android.externalstorage.documents/document/primary:$encodedPath".toUri()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(folderUri, "vnd.android.document/directory")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }
}
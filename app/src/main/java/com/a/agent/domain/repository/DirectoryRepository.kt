package com.a.agent.domain.repository

import com.a.agent.domain.model.Directory
import java.io.File

interface DirectoryRepository {
    fun initializeDirectory()
    fun setDirectory(directory: Directory, fileName: String): File

    fun openFolder(targetPath: String)
}
package com.a.agent.data.mapper

import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.Model
import com.a.agent.presentation.util.toMegaByte
import java.io.File

fun ModelEntity.toModel(): Model {
    val expectedModelFileSize = this.totalBytes.toMegaByte()
    val modelFileSize = File(this.path).length().toMegaByte()

    val model = Model(
        id = this.id,
        name = this.name,
        url = this.url,
        path = this.path,
        fileName = this.fileName,
        totalBytes = this.totalBytes,
        modelSource = this.modelSource,
        isDownloaded = expectedModelFileSize == modelFileSize
    )

    return model
}
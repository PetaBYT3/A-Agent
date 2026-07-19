package com.a.agent.data.mapper

import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.Model
import java.io.File

fun ModelEntity.toModel(): Model = Model(
    id = this.id,
    name = this.name,
    url = this.url,
    path = this.path,
    fileName = this.fileName,
    totalBytes = this.totalBytes,
    isDownloaded = File(this.path).exists()
)
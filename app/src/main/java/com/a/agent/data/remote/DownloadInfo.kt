package com.a.agent.data.remote

data class DownloadInfo(
    val totalBytes: Long,
    val downloadedBytes: Long,
    val progress: Float,
    val percentage: Int
)

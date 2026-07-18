package com.a.agent.data.remote

data class DownloadInfo(
    val totalBytes: Long,
    val downloadedBytes: Long,
    val progress: Float,
    val percentage: Int
) {
    companion object {
        val Empty = DownloadInfo(
            totalBytes = 0,
            downloadedBytes = 0,
            progress = 0f,
            percentage = 0
        )
    }
}

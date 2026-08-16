package com.a.agent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.a.agent.BuildConfig
import com.a.agent.R
import com.a.agent.domain.repository.LlmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DownloadService: Service() {
    private val llmRepository: LlmRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager

    private var activeChildNotificationsId = mutableSetOf<Int>()

    companion object {
        const val SUMMARY_NOTIFICATION_ID = 99
        const val CHANNEL_ID = "downloadChannel"
        const val GROUP_KEY = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_GROUP"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val notification = buildSummaryNotification(0)
        ServiceCompat.startForeground(
            this,
            SUMMARY_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
        serviceScope.launch {
            llmRepository.activeDownloadMap.collect { map ->
                if (map.isNotEmpty()) {
                    val summaryNotification = buildSummaryNotification(map.size)
                    notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)

                    val currentId = mutableSetOf<Int>()
                    map.forEach { (id, pair) ->
                        val childId = id.hashCode()
                        currentId.add(childId)

                        val childNotification = buildChildNotification(pair.first, pair.second.percentage)
                        notificationManager.notify(childId, childNotification)
                    }

                    val removeId = activeChildNotificationsId - currentId
                    removeId.forEach { id ->
                        notificationManager.cancel(id)
                    }
                    activeChildNotificationsId = currentId
                } else {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun buildSummaryNotification(totalFiles: Int): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (totalFiles > 1) "Downloading $totalFiles Files" else "Downloading")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

        return notification
    }

    private fun buildChildNotification(fileName: String, progress: Int): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(fileName)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(false)
            .build()

        return notification
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Download LLM", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}
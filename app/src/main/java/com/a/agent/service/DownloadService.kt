package com.a.agent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.a.agent.R
import com.a.agent.domain.repository.ModelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DownloadService: Service() {
    private val modelRepository: ModelRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val NOTIFICATION_ID = 99
        const val CHANNEL_ID = "downloadChannel"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val notification = buildNotification(0)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
        serviceScope.launch {
            modelRepository.downloadState.collect { map ->
                if (map.isNotEmpty()) {
                    val avgPercentage = map.values.map { it.percentage }.average().toInt()
                    val notification = buildNotification(
                        progress = avgPercentage
                    )
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
        }

        return START_STICKY
    }

    private fun buildNotification(progress: Int): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        return notification
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Download Models", NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}
package com.simform.audio_waveforms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    private const val CHANNEL_ID = "audio_recording_channel"
    private const val CHANNEL_NAME = "Audio Recording"
    private const val CHANNEL_DESCRIPTION = "Channel for audio recording notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context): Notification {
        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("Recording Audio")
            .setContentText("Your audio is being recorded")
            .setSmallIcon(android.R.drawable.ic_menu_mic)
            .setOngoing(true)
            .build()
    }
}
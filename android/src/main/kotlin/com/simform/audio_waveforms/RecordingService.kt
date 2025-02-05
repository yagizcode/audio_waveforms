package com.simform.audio_waveforms

import android.app.Notification
import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log

private const val CHANNEL_ID = "recording_service_channel"

class RecordingService : Service() {

    override fun onCreate() {
        super.onCreate()

        // Create a notification channel for foreground service (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recording Service"
            val descriptionText = "Recording audio in the background"
            val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            val notificationManager = getSystemService(NotificationManagerCompat::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Create a persistent notification to show when the service is active
        val notification = createRecordingNotification()
        startForeground(1, notification)  // Start the service as a foreground service

        return START_STICKY  // Keep the service running until explicitly stopped
    }

    private fun createRecordingNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording in Progress")
            .setContentText("Recording audio...")
            .setSmallIcon(R.drawable.ic_record)  // Make sure you have an icon for the notification
            .setOngoing(true)  // Keep it ongoing so the user can't dismiss it
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)  // Stop the foreground service and remove the notification
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null  // No binding needed
    }
}

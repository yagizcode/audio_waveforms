package com.simform.audio_waveforms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Microphone is Active")
            .setContentText("This app is accessing your microphone.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start your microphone handling here
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop recording audio here
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}
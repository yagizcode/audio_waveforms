package com.simform.audio_waveforms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MicService : Service() {
    
    companion object {
        const val CHANNEL_ID = "MicServiceChannel"
    }

    private var recorder: MediaRecorder? = null
    private var filePath: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording")
            .setContentText("SmartNoter is recording.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Mic Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        filePath = intent?.getStringExtra("filePath")
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        if (filePath.isNullOrEmpty()) {
            Log.e("MicService", "File path is empty, cannot start recording")
            return
        }

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(filePath)
            try {
                prepare()
                start()
                Log.d("MicService", "Recording started")
            } catch (e: Exception) {
                Log.e("MicService", "Failed to start recording", e)
            }
        }
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                reset()
                release()
                Log.d("MicService", "Recording stopped")
            } catch (e: RuntimeException) {
                Log.e("MicService", "Error stopping recorder", e)
            }
        }
        recorder = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

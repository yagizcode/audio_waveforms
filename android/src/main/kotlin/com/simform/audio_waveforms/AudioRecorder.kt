package com.simform.audio_waveforms

import android.content.Context
import android.content.Intent
import android.util.Log

class AudioRecorder {
    private var filePath: String? = null

    fun initRecorder(path: String) {
        filePath = path
    }

    fun startRecorder(context: Context) {
        if (filePath.isNullOrEmpty()) {
            Log.e("AudioRecorder", "File path is empty, cannot start recording")
            return
        }

        val intent = Intent(context, MicService::class.java).apply {
            action = MicService.ACTION_START
            putExtra(MicService.EXTRA_FILE_PATH, filePath)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopRecording(context: Context) {
        val intent = Intent(context, MicService::class.java).apply {
            action = MicService.ACTION_STOP
        }
        context.startService(intent)
    }
}

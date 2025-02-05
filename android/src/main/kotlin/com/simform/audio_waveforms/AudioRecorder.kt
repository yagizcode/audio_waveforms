package com.simform.audio_waveforms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import io.flutter.plugin.common.MethodChannel

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

    fun checkPermission(result: MethodChannel.Result, activity: Activity?) {
        if (activity == null) {
            result.error("ACTIVITY_NULL", "Activity is null, cannot request permissions", null)
            return
        }

        val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
        if (!isPermissionGranted(activity)) {
            ActivityCompat.requestPermissions(activity, permissions, 1001)
        } else {
            result.success(true)
        }
    }

    private fun isPermissionGranted(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

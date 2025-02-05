package com.simform.audio_waveforms

import android.app.Activity
import android.content.Intent
import android.media.MediaRecorder
import android.util.Log
import java.io.File

class AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var filePath: String? = null
    
    fun initRecorder(path: String) {
        filePath = path
    }

    fun startRecorder(activity: Activity?) {
        if (filePath.isNullOrEmpty()) {
            Log.e("AudioRecorder", "File path is empty, cannot start recording")
            return
        }
        
        val intent = Intent(activity, MicService::class.java)
        intent.putExtra("filePath", filePath)
        activity?.startService(intent)
    }

    fun stopRecording(activity: Activity?) {
        val intent = Intent(activity, MicService::class.java)
        activity?.stopService(intent)
    }
}

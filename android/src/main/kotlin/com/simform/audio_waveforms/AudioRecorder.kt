package com.simform.audio_waveforms

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import kotlin.math.log10

private const val LOG_TAG = "AudioWaveforms"
private const val RECORD_AUDIO_REQUEST_CODE = 1001
private const val CHANNEL_ID = "recording_service_channel"

class AudioRecorder : PluginRegistry.RequestPermissionsResultListener {
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var useLegacyNormalization = false
    private var successCallback: RequestPermissionsSuccessCallback? = null

    fun startRecorder(result: MethodChannel.Result, recorder: MediaRecorder?, useLegacy: Boolean, context: Context) {
        try {
            useLegacyNormalization = useLegacy
            recorder?.start()
            result.success(true)

            // Start the foreground service to keep the microphone active
            val serviceIntent = Intent(context, RecordingService::class.java)
            context.startService(serviceIntent)  // Start the service to keep recording in the background
        } catch (e: IllegalStateException) {
            Log.e(LOG_TAG, "Failed to start recording")
        }
    }

    fun stopRecording(result: MethodChannel.Result, recorder: MediaRecorder?, path: String, context: Context) {
        try {
            val hashMap: HashMap<String, Any?> = HashMap()
            try {
                recorder?.stop()

                val duration = getDuration(path)

                hashMap[Constants.resultFilePath] = path
                hashMap[Constants.resultDuration] = duration
            } catch (e: RuntimeException) {
                // Stop was called immediately after start which causes stop() call to fail.
                hashMap[Constants.resultFilePath] = null
                hashMap[Constants.resultDuration] = -1
            }

            recorder?.apply {
                reset()
                release()
            }

            // Stop the foreground service once recording is complete
            val serviceIntent = Intent(context, RecordingService::class.java)
            context.stopService(serviceIntent)

            result.success(hashMap)
        } catch (e: IllegalStateException) {
            Log.e(LOG_TAG, "Failed to stop recording")
        }
    }

    private fun getDuration(path: String): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaMetadataRetriever.setDataSource(path)
            val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return duration?.toInt() ?: -1
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to get recording duration")
        } finally {
            mediaMetadataRetriever.release()
        }
        return -1
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ): Boolean {
        return if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            successCallback?.onSuccess(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    private fun isPermissionGranted(activity: Activity?): Boolean {
        val result =
                ActivityCompat.checkSelfPermission(activity!!, permissions[0])
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermission(result: MethodChannel.Result, activity: Activity?, successCallback: RequestPermissionsSuccessCallback) {
        this.successCallback = successCallback
        if (!isPermissionGranted(activity)) {
            activity?.let {
                ActivityCompat.requestPermissions(
                        it, permissions,
                        RECORD_AUDIO_REQUEST_CODE
                )
            }
        } else {
            result.success(true)
        }
    }
}

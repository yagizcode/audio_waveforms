package com.simform.audio_waveforms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** AudioWaveformsPlugin */
class AudioWaveformsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var recorder: MediaRecorder? = null
    private var activity: Activity? = null
    private lateinit var audioRecorder: AudioRecorder
    private var recorderSettings = RecorderSettings(path = null, bitRate = null)
    private lateinit var applicationContext: Context
    private var audioPlayers = mutableMapOf<String, AudioPlayer?>()
    private var extractors = mutableMapOf<String, WaveformExtractor?>()
    private var pluginBinding: ActivityPluginBinding? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, Constants.methodChannelName)
        channel.setMethodCallHandler(this)
        audioRecorder = AudioRecorder()
        applicationContext = flutterPluginBinding.applicationContext
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            Constants.initRecorder -> {
                val arguments = call.arguments
                if (arguments != null && arguments is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    recorderSettings = RecorderSettings.fromJson(json = arguments as Map<String, Any?>)
                    checkPathAndInitialiseRecorder(result, recorderSettings)
                } else {
                    result.error(Constants.LOG_TAG, "Failed to initialise Recorder", "Invalid Arguments")
                }
            }

            Constants.startRecording -> {
                val useLegacyNormalization =
                    (call.argument(Constants.useLegacyNormalization) as Boolean?) ?: false
                audioRecorder.startRecorder(result, recorder, useLegacyNormalization)

                // Start Foreground Service for background recording
                val filePath = recorderSettings.path
                if (filePath != null) {
                    val intent = Intent(applicationContext, MicService::class.java).apply {
                        action = MicService.ACTION_START
                        putExtra(MicService.EXTRA_FILE_PATH, filePath)
                    }
                    applicationContext.startForegroundService(intent)
                }
            }

            Constants.stopRecording -> {
                audioRecorder.stopRecording(result, recorder, recorderSettings.path!!)
                recorder = null

                // Stop Foreground Service
                val intent = Intent(applicationContext, MicService::class.java).apply {
                    action = MicService.ACTION_STOP
                }
                applicationContext.startService(intent)
            }

            Constants.pauseRecording -> audioRecorder.pauseRecording(result, recorder)
            Constants.resumeRecording -> audioRecorder.resumeRecording(result, recorder)
            Constants.getDecibel -> audioRecorder.getDecibel(result, recorder)
            Constants.checkPermission -> audioRecorder.checkPermission(result, activity, result::success)

            else -> result.notImplemented()
        }
    }

    private fun checkPathAndInitialiseRecorder(result: Result, recorderSettings: RecorderSettings) {
        try {
            recorder = MediaRecorder()
        } catch (e: Exception) {
            Log.e(Constants.LOG_TAG, "Failed to initialise Recorder")
        }
        if (recorderSettings.path == null) {
            val outputDir = activity?.cacheDir
            val outputFile: File?
            val dateTimeInstance = SimpleDateFormat(Constants.fileNameFormat, Locale.US)
            val currentDate = dateTimeInstance.format(Date())
            try {
                outputFile = File.createTempFile(currentDate, ".m4a", outputDir)
                recorderSettings.path = outputFile.path
                audioRecorder.initRecorder(result, recorder, recorderSettings)
            } catch (e: IOException) {
                Log.e(Constants.LOG_TAG, "Failed to create file")
            }
        } else {
            audioRecorder.initRecorder(result, recorder, recorderSettings)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        pluginBinding = binding
        pluginBinding!!.addRequestPermissionsResultListener(this.audioRecorder)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        recorder?.release()
        recorder = null
        audioPlayers.clear()
        extractors.clear()
        activity = null
        pluginBinding?.removeRequestPermissionsResultListener(this.audioRecorder)
    }
}

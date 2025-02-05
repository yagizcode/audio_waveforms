package com.simform.audio_waveforms

import android.app.Activity
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

class AudioWaveformsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var recorder: MediaRecorder? = null
    private var activity: Activity? = null
    private lateinit var audioRecorder: AudioRecorder
    private var recorderSettings = RecorderSettings(path = null, bitRate = null)
    private lateinit var applicationContext: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, Constants.methodChannelName)
        channel.setMethodCallHandler(this)
        audioRecorder = AudioRecorder()
        applicationContext = flutterPluginBinding.applicationContext

        // Add method channel for foreground service
        MethodChannel(flutterPluginBinding.binaryMessenger, "com.simform.audio_waveforms/foreground_service").setMethodCallHandler { call, result ->
            when (call.method) {
                "startForegroundService" -> {
                    val intent = Intent(applicationContext, ForegroundService::class.java)
                    applicationContext.startService(intent)
                    result.success(null)
                }
                "stopForegroundService" -> {
                    val intent = Intent(applicationContext, ForegroundService::class.java)
                    applicationContext.stopService(intent)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            Constants.startForegroundService -> {
                val intent = Intent(activity, MicService::class.java)
                activity?.startForegroundService(intent)
                result.success(true)
            }

            Constants.stopForegroundService -> {
                val intent = Intent(activity, MicService::class.java)
                activity?.stopService(intent)
                result.success(true)
            }

            Constants.initRecorder -> {
                // Existing initialization logic...
            }

            Constants.startRecording -> {
                audioRecorder.startRecorder(result, recorder, false)
            }

            Constants.stopRecording -> {
                audioRecorder.stopRecording(result, recorder, recorderSettings.path!!)
                recorder = null
            }

            // Other existing methods...

            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
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
    }
}
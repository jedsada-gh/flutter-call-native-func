package com.example.my_flutter_application

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.util.Random

class MainActivity : FlutterActivity() {
  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var channel: MethodChannel

  override fun onStop() {
    methodChannel.invokeMethod(MethodName.DISABLE_EVENT_RECEIVER, null)
    super.onStop()
  }

  override fun onDestroy() {
    methodChannel.invokeMethod(MethodName.DISABLE_EVENT_RECEIVER, null)
    super.onDestroy()
  }

  @Deprecated("Deprecated in Java")
  override fun onBackPressed() {
    methodChannel.invokeMethod(MethodName.DISABLE_EVENT_RECEIVER, null)
    super.onBackPressed()
  }

  fun getRandomNumber(): Int {
    return Random().nextInt()
  }

  override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)
    methodChannel =
      MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MethodChannelName.DEFAULT)

    channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MethodChannelName.DEFAULT)
    channel.setMethodCallHandler { call, result ->
      when (call.method) {
        MethodName.RANDOM_NUMBER -> {
          val resultNumber = getRandomNumber()
          if (resultNumber != -1) {
            result.success(resultNumber)
          } else {
            result.error("UNAVAILABLE", "Random number not available.", null)
          }
        }

        MethodName.NAVIGATE_TO_FACE_DETECTION_PAGE -> {
          startActivity(Intent(this, FaceDetectionActivity::class.java))
        }

        else -> result.notImplemented()
      }
    }

    eventChannel =
      EventChannel(flutterEngine.dartExecutor.binaryMessenger, EventChannelName.DEFAULT)
    eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        Log.i("Android", "EventChannel onListen called")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
          override fun run() {
            eventSink?.success(Gson().toJson(Event("randomNumber", getRandomNumber())))
            // eventSink?.endOfStream()
            // eventSink?.error("error code", "error message","error details")
            mainHandler.postDelayed(this, 1000)
          }
        })
      }

      override fun onCancel(arguments: Any?) {
        Log.i("Android", "EventChannel onCancel called")
      }
    })
  }
}

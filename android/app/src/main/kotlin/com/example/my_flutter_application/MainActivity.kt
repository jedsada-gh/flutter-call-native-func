package com.example.my_flutter_application

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.util.Random

class MainActivity : FlutterActivity() {
  private val CHANNEL = "com.example.my_flutter_application/random_number"
  private val METHOD_RANDOM_NUMBER = "getRandomNumber"

  private val EVENT_CHANNEL = "com.example.my_flutter_application/event"
  private lateinit var eventChannel: EventChannel

  private lateinit var channel: MethodChannel

  override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)

    fun getRandomNumber(): Int {
      return Random().nextInt()
    }

    channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
    channel.setMethodCallHandler { call, result ->
      when (call.method) {
        METHOD_RANDOM_NUMBER -> {
          val resultNumber = getRandomNumber()
          if (resultNumber != -1) {
            result.success(resultNumber)
          } else {
            result.error("UNAVAILABLE", "Random number not available.", null)
          }
        }

        else -> result.notImplemented()
      }
    }

    eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)
    eventChannel.setStreamHandler(object: EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        Log.i("Android", "EventChannel onListen called")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
          override fun run() {
            eventSink?.success("${getRandomNumber()}")
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

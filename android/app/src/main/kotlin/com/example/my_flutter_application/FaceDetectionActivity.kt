package com.example.my_flutter_application

import android.content.pm.PackageManager
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.Manifest
import androidx.core.app.ActivityCompat
import com.example.my_flutter_application.extension.requestPermission
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor
import com.google.gson.Gson
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.io.IOException

class FaceDetectionActivity : FlutterActivity() {

  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var surfaceView: SurfaceView
  private var cameraSource: CameraSource? = null
  private var eventSink: EventChannel.EventSink? = null

  override fun onResume() {
    super.onResume()
    methodChannel.invokeMethod(MethodName.ENABLE_EVENT_RECEIVER, null)
  }

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_face_detection)
    surfaceView = findViewById(R.id.surfaceView)

    val detector = FaceDetector.Builder(this)
      .setProminentFaceOnly(true) // optimize for single, relatively large face
      .setTrackingEnabled(true) // enable face tracking
      .setClassificationType( /* eyes open and smile */FaceDetector.ALL_CLASSIFICATIONS)
      .setMode(FaceDetector.FAST_MODE) // for one face this is OK
      .build()


    cameraSource =
      CameraSource.Builder(this, detector).setFacing(CAMERA_FACING_FRONT).setRequestedFps(2.0f)
        .setRequestedPreviewSize(1280, 960).setAutoFocusEnabled(true).build();

    surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
      override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        try {
          requestPermission(listOf(
            Manifest.permission.CAMERA
          ), {
            if (ActivityCompat.checkSelfPermission(
                this@FaceDetectionActivity, Manifest.permission.CAMERA
              ) != PackageManager.PERMISSION_GRANTED
            ) {
              // TODO: Consider calling
              //    ActivityCompat#requestPermissions
              // here to request the missing permissions, and then overriding
              //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
              //                                          int[] grantResults)
              // to handle the case where the user grants the permission. See the documentation
              // for ActivityCompat#requestPermissions for more details.
              return@requestPermission
            }
            cameraSource?.start(surfaceView.holder)
            detector.setProcessor(
              LargestFaceFocusingProcessor(detector, FaceGraphic(object : FaceGraphicListener {
                override fun onEyeBlinking() {
                  runOnUiThread {
                    eventSink?.success(Gson().toJson(Event("faceDetection", "eye blinking")))
                  }
                }

                override fun onFaceSmiling() {
                  runOnUiThread {
                    eventSink?.success(Gson().toJson(Event("faceDetection", "face smiling")))
                  }
                }

                override fun onFaceTurnRight() {
                  runOnUiThread {
                    eventSink?.success(Gson().toJson(Event("faceDetection", "face turn right")))
                  }
                }

                override fun onFaceTurnLeft() {
                  runOnUiThread {
                    eventSink?.success(Gson().toJson(Event("faceDetection", "face turn left")))
                  }
                }
              }))
            )
          }, { print("Permission is denied") })
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }

      override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}
      override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        cameraSource?.stop()
      }
    })
  }

  override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)
    methodChannel =
      MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MethodChannelName.DEFAULT)

    eventChannel =
      EventChannel(flutterEngine.dartExecutor.binaryMessenger, EventChannelName.DEFAULT)
    eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, event: EventChannel.EventSink?) {
        Log.i("Android", "FaceDetectionEventChannel onListen called")
        eventSink = event
      }

      override fun onCancel(arguments: Any?) {
        Log.i("Android", "FaceDetectionEventChannel onCancel called")
      }
    })
  }
}
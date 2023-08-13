package com.example.my_flutter_application

import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face

interface FaceGraphicListener {
  fun onEyeBlinking()
  fun onFaceSmiling()
  fun onFaceTurnRight()
  fun onFaceTurnLeft()
}

class FaceGraphic(private val listener: FaceGraphicListener) : Tracker<Face>() {
  private val OPEN_THRESHOLD = 0.85f
  private val CLOSE_THRESHOLD = 0.4f
  private val FACE_TURN_THRESHOLD = 50f
  private val SMILING_THRESHOLD0 = 0.7f
  private var state = 0

  private fun blink(value: Float) {
    when (state) {
      0 -> if (value > OPEN_THRESHOLD) {
        // Both eyes are initially open
        state = 1
      }

      1 -> if (value < CLOSE_THRESHOLD) {
        // Both eyes become closed
        state = 2
      }

      2 -> if (value > OPEN_THRESHOLD) {
        // Both eyes are open again
        listener.onEyeBlinking()
        state = 0
      }
    }
  }

  /**
   * Update the position/characteristics of the face within the overlay.
   */
  override fun onUpdate(detectionResults: Detector.Detections<Face>, face: Face) {
    val left: Float = face.isLeftEyeOpenProbability
    val right: Float = face.isRightEyeOpenProbability
    if (left == Face.UNCOMPUTED_PROBABILITY || right == Face.UNCOMPUTED_PROBABILITY) {
      // One of the eyes was not detected.
      return
    }

    if (face.isSmilingProbability >= SMILING_THRESHOLD0) {
      listener.onFaceSmiling()
    }

    if (face.eulerY >= FACE_TURN_THRESHOLD) {
      listener.onFaceTurnLeft()
    }

    if (face.eulerY <= -FACE_TURN_THRESHOLD) {
      listener.onFaceTurnRight()
    }

    val value = left.coerceAtMost(right)
    blink(value)
  }
}
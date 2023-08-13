package com.example.my_flutter_application

object EventChannelName {
  const val DEFAULT = "com.example.my_flutter_application/event"
}

object MethodChannelName {
  const val DEFAULT = "com.example.my_flutter_application/bridge_native"
}

object MethodName {
  const val RANDOM_NUMBER = "getRandomNumber"
  const val NAVIGATE_TO_FACE_DETECTION_PAGE = "navigateToFaceDetectionPage"
  const val ENABLE_EVENT_RECEIVER = "enableEventReceiver"
  const val DISABLE_EVENT_RECEIVER = "disableEventReceiver"
}
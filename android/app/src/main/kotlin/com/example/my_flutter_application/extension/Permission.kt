package com.example.my_flutter_application.extension

import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

fun Activity.requestPermission(
  permissions: List<String>, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit
) {
  Dexter.withActivity(this).withPermissions(permissions)
    .withListener(object : MultiplePermissionsListener {
      override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        when (report?.areAllPermissionsGranted() == true) {
          true -> onPermissionGranted.invoke()
          else -> onPermissionDenied.invoke()
        }
      }

      override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?, token: PermissionToken?
      ) {
        token?.continuePermissionRequest()
      }
    }).check()
}
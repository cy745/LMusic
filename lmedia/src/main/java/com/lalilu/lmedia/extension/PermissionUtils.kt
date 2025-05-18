package com.lalilu.lmedia.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun check(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context.applicationContext, permission) !=
                PackageManager.PERMISSION_DENIED
}
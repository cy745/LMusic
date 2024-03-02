package com.lalilu.extension_core

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable


val EMPTY_CONTENT = @Composable {}

object Content {
    const val COMPONENT_HOME = "component_home"
    const val COMPONENT_CATEGORY = "component_category"
    const val COMPONENT_SETTINGS = "component_settings"
    const val COMPONENT_MAIN = "component_main"
    const val COMPONENT_DETAIL = "component_detail"

    const val PARAMS_MEDIA_ID = "mediaId"
}

internal object Constants {
    const val EXTENSION_FEATURE_NAME = "lmusic.extension"
    const val EXTENSION_META_DATA_CLASS = "lmusic.extension.class"
    const val EXTENSION_SOURCES_CLASS = "lalilu.extension_ksp.ExtensionsConstants"
    val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or
            PackageManager.GET_META_DATA or
            PackageManager.GET_SIGNATURES or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else 0)
}
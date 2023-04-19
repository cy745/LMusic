package com.lalilu.common

import android.annotation.SuppressLint
import java.lang.reflect.Method

@SuppressLint("PrivateApi")
object CustomRomUtils {
    private const val VERSION_PROPERTY_FLYME = "ro.build.display.id"

    private val sysClass: Class<*> by lazy { Class.forName("android.os.SystemProperties") }
    private val getStringMethod: Method by lazy {
        sysClass.getDeclaredMethod("get", String::class.java)
    }

    val isFlyme: Boolean by lazy {
        val result = getStringMethod.invoke(sysClass, VERSION_PROPERTY_FLYME) as String?
        result?.uppercase()?.contains("FLYME") == true
    }
}
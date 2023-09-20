package com.lalilu.crash

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.io.PrintWriter
import java.io.StringWriter


object CrashHelper : Thread.UncaughtExceptionHandler {

    private lateinit var mContext: Application
    private lateinit var packageManager: PackageManager

    fun init(context: Application) {
        mContext = context
        packageManager = context.packageManager
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!this::mContext.isInitialized) return

        try {
            handleException(e)
        } catch (exception: Exception) {
            handleException(exception)
        }

        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun handleException(e: Throwable) {
        e.printStackTrace()
        Intent(mContext, CrashActivity::class.java).run {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            e.toCrashModel().copyTo(this)
            mContext.startActivity(this)
        }
    }

    fun Throwable.toCrashModel(): CrashModel {
        val causeClass = this.cause ?: this

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        causeClass.printStackTrace(pw)
        pw.flush()

        val stackTrackElement =
            stackTrace.firstOrNull { it.className.contains(mContext.packageName) }
                ?: stackTrace.getOrNull(0)

        val buildVersion = kotlin.runCatching {
            packageManager.getPackageInfo(mContext.packageName, 0)
                .let { "[${it.versionCode}:${it.versionName}]" }
        }.getOrNull()

        val deviceInfo = "[${Build.MODEL} : ${Build.BRAND} : ${Build.DEVICE}]\n" +
                "[API${Build.VERSION.SDK_INT} : ${Build.VERSION.RELEASE}]\n" +
                "[${Build.CPU_ABI}]"

        return CrashModel(
            title = causeClass.javaClass.name,
            message = causeClass.message ?: "No Message.",
            causeClass = stackTrackElement?.className ?: "",
            causeFile = stackTrackElement?.fileName ?: "",
            causeMethod = stackTrackElement?.methodName ?: "",
            causeLine = stackTrackElement?.lineNumber.toString(),
            stackTrace = sw.toString().take(4000),
            deviceInfo = deviceInfo,
            buildVersion = buildVersion ?: ""
        )
    }
}
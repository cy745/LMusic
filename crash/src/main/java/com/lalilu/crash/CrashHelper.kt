package com.lalilu.crash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object CrashHelper : Thread.UncaughtExceptionHandler {
    const val EXTRA_KEY = "CRASH_MODEL"
    private lateinit var mContext: Application
    private lateinit var packageManager: PackageManager
    private val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    fun init(context: Application) {
        mContext = context
        packageManager = context.packageManager
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!this::mContext.isInitialized) return

        e.printStackTrace()
        LogUtils.e(e)

        try {
            handleException(e)
        } catch (exception: Exception) {
            handleException(exception)
        }

        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun handleException(e: Throwable) {
        Intent(mContext, CrashActivity::class.java).run {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_KEY, e.toCrashModel())
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

    private fun getFileUri(context: Context, file: File): Uri? = FileProvider
        .getUriForFile(context, context.applicationContext.packageName + ".fileprovider", file)

    suspend fun shareLog(activity: Activity, crashModel: CrashModel? = null) =
        withContext(Dispatchers.IO) {
            val cacheDir = activity.cacheDir
            val tempDirectory = File("${cacheDir}/crash_temp")
            if (tempDirectory.exists()) tempDirectory.deleteRecursively()
            if (!tempDirectory.exists()) tempDirectory.mkdir()

            val timeStr = dateFormatter.format(Date(System.currentTimeMillis()))
            val logDirectory = File("${cacheDir}/log")
            val infoFile = File(tempDirectory, "crash_info.json")
            val zipFile = File(tempDirectory, "share_log_${timeStr}.zip")
            val zipFileUri = getFileUri(activity, zipFile)

            if (!logDirectory.exists() || !logDirectory.isDirectory || !logDirectory.canRead() || zipFileUri == null) {
                withContext(Dispatchers.Main) { ToastUtils.showLong("日志文件不存在或无法读取") }
                return@withContext
            }

            if (!infoFile.exists()) infoFile.createNewFile()
            infoFile.writeText(GsonUtils.toJson(crashModel))

            if (!zipFile.exists()) zipFile.createNewFile()
            ZipUtils.zipFiles(listOf(logDirectory, infoFile), zipFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, zipFileUri)
                putExtra(Intent.EXTRA_TITLE, "崩溃日志分享")
            }
            withContext(Dispatchers.Main) {
                activity.startActivity(Intent.createChooser(intent, "分享日志文件"))
            }
        }
}
package com.lalilu.crash

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TITLE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.ZipUtils
import com.lalilu.crash.databinding.ActivityCrashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext


class CrashActivity : ComponentActivity(), CoroutineScope, View.OnClickListener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val binding by lazy { ActivityCrashBinding.inflate(layoutInflater) }
    private var crashModel: CrashModel? = null
    private val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        bindView()
    }

    @Suppress("DEPRECATION")
    private fun bindView() {
        binding.restartBtn.setOnClickListener(this)
        binding.exitBtn.setOnClickListener(this)
        binding.shareLogBtn.setOnClickListener(this)

        crashModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(CrashHelper.EXTRA_KEY, CrashModel::class.java)
        } else {
            intent.getParcelableExtra(CrashHelper.EXTRA_KEY)
        }

        binding.crashTitle.text = crashModel?.title
        binding.crashMessage.text = crashModel?.message
        binding.crashClassName.text = crashModel?.causeClass
        binding.crashFile.text = crashModel?.causeFile
        binding.crashLine.text = crashModel?.causeLine
        binding.crashMethod.text = crashModel?.causeMethod
        binding.crashAndroidVersion.text = crashModel?.buildVersion
        binding.crashDeviceInfo.text = crashModel?.deviceInfo
        binding.crashStacktrace.text = crashModel?.stackTrace
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.restart_btn -> {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                finish()
            }

            R.id.exit_btn -> {
                finishAffinity()
            }

            R.id.share_log_btn -> {
                launch {
                    val tempDirectory = File("${cacheDir}/crash_temp")
                    if (tempDirectory.exists()) tempDirectory.deleteRecursively()
                    if (!tempDirectory.exists()) tempDirectory.mkdir()

                    val timeStr = dateFormatter.format(Date(System.currentTimeMillis()))
                    val logDirectory = File("${cacheDir}/log")
                    val infoFile = File(tempDirectory, "crash_info.json")
                    val zipFile = File(tempDirectory, "share_log_${timeStr}.zip")
                    val zipFileUri = getFileUri(this@CrashActivity, zipFile)

                    if (!logDirectory.exists() || !logDirectory.isDirectory || !logDirectory.canRead() || zipFileUri == null) {
                        withContext(Dispatchers.Main) { ToastUtils.showLong("日志文件不存在或无法读取") }
                        return@launch
                    }

                    if (!infoFile.exists()) infoFile.createNewFile()
                    infoFile.writeText(GsonUtils.toJson(crashModel))

                    if (!zipFile.exists()) zipFile.createNewFile()
                    ZipUtils.zipFiles(listOf(logDirectory, infoFile), zipFile)

                    val intent = Intent(ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(EXTRA_STREAM, zipFileUri)
                        putExtra(EXTRA_TITLE, "崩溃日志分享")
                    }
                    withContext(Dispatchers.Main) {
                        startActivity(Intent.createChooser(intent, "分享日志文件"))
                    }
                }
            }
        }
    }

    private fun getFileUri(context: Context, file: File): Uri? = FileProvider
        .getUriForFile(context, context.applicationContext.packageName + ".fileprovider", file)
}
package com.lalilu.crash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.lalilu.crash.databinding.ActivityCrashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class CrashActivity : ComponentActivity(), CoroutineScope, View.OnClickListener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val binding by lazy { ActivityCrashBinding.inflate(layoutInflater) }
    private var crashModel: CrashModel? = null

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

            R.id.share_log_btn -> launch {
                CrashHelper.shareLog(this@CrashActivity, crashModel)
            }
        }
    }
}
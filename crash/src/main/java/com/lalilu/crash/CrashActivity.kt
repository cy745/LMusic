package com.lalilu.crash

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.lalilu.crash.databinding.ActivityCrashBinding

class CrashActivity : ComponentActivity(), View.OnClickListener {

    private val binding by lazy {
        ActivityCrashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        bindView()
    }

    @Suppress("DEPRECATION")
    private fun bindView() {
        binding.restartBtn.setOnClickListener(this)
        binding.exitBtn.setOnClickListener(this)

        val crashModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(CrashHelper.EXTRA_KEY, CrashModel::class.java)
        } else {
            intent.getParcelableExtra(CrashHelper.EXTRA_KEY)
        } ?: return

        binding.crashTitle.text = crashModel.title
        binding.crashMessage.text = crashModel.message
        binding.crashClassName.text = crashModel.causeClass
        binding.crashFile.text = crashModel.causeFile
        binding.crashLine.text = crashModel.causeLine
        binding.crashMethod.text = crashModel.causeMethod
        binding.crashAndroidVersion.text = crashModel.buildVersion
        binding.crashDeviceInfo.text = crashModel.deviceInfo
        binding.crashStacktrace.text = crashModel.stackTrace
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.restart_btn -> {
                android.os.Process.killProcess(android.os.Process.myPid())
            }

            R.id.exit_btn -> {
                finishAffinity()
            }
        }
    }
}
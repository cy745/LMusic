package com.lalilu.crash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lalilu.crash.databinding.ActivityCrashBinding

class CrashActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCrashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        bindView()
    }

    private fun bindView() {
        val crashModel = intent.toCrashModel()

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
}
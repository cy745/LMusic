package com.lalilu.lmusic.base

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media.MediaBrowserServiceCompat

@Deprecated("替换为使用Hilt注入ViewModel，不需要自行创建ViewModelProvider之类的了")
abstract class BaseService : MediaBrowserServiceCompat() {
    private var mApplicationProvider: ViewModelProvider? = null
    open fun initViewModel() {}
    open fun loadInitData() {}

    override fun onCreate() {
        super.onCreate()
        initViewModel()
    }

    protected fun <T : ViewModel> getApplicationViewModel(modelClass: Class<T>): T {
        if (mApplicationProvider == null) {
            mApplicationProvider =
                ViewModelProvider(
                    application as BaseApplication,
                    getAppFactory()
                )
        }
        return mApplicationProvider!![modelClass]
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        return ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("normal", null)
    }
}
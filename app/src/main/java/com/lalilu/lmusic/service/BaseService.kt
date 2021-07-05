package com.lalilu.lmusic.service

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media.MediaBrowserServiceCompat
import com.lalilu.lmusic.base.BaseApplication

abstract class BaseService : MediaBrowserServiceCompat() {
    private var mApplicationProvider: ViewModelProvider? = null
    open fun initViewModel() {}
    open fun loadInitData() {}

    override fun onCreate() {
        super.onCreate()
        initViewModel()
    }

    protected fun <T : ViewModel?> getApplicationViewModel(modelClass: Class<T>): T {
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

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        loadInitData()
        result.sendResult(ArrayList())
    }
}
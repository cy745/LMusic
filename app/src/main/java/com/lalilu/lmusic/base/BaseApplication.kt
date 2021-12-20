package com.lalilu.lmusic.base

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

@Deprecated("替换为使用Hilt注入ViewModel，不需要使用该BaseApplication自建ViewModelStore")
open class BaseApplication : Application(), ViewModelStoreOwner {
    private var mAppViewModelStore: ViewModelStore? = null

    override fun onCreate() {
        super.onCreate()
        mAppViewModelStore = ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore = mAppViewModelStore!!
}
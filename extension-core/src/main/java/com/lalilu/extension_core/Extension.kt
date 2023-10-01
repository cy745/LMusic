package com.lalilu.extension_core

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

interface Extension : LifecycleEventObserver {
    /**
     * 首页模块中的内容
     */
    val homeContent: @Composable () -> Unit

    /**
     * 插件的主页面
     */
    val mainContent: @Composable () -> Unit

    /**
     * 插件列表页的banner
     */
    val bannerContent: @Composable () -> Unit

    /**
     * 监听宿主Activity的状态变化
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {}
}
package com.lalilu.extension_core

import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Keep
interface Extension : LifecycleEventObserver {

    /**
     * 注册返回内容提供器
     *
     * @return 返回空则意味无内容提供能力
     */
    @Keep
    fun getProvider(): Provider? = null

    /**
     * 注册自定义的界面供宿主访问调用
     */
    @Keep
    fun getContentMap(): Map<String, @Composable (Map<String, String>) -> Unit>

    /**
     * 监听宿主Activity的状态变化
     */
    @Keep
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    }
}
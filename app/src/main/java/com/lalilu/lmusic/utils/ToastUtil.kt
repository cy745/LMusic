package com.lalilu.lmusic.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ToastUtil {
    private var lastText: String? = null
    private var nextText: String? = null
    private var refuseTime: Long = 0
    private var restartTimeout: Long = 2000

    fun text(text: String): ToastUtil {
        nextText = text
        return this
    }

    /**
     * 内容不变拒绝展示 Toast，记录拒绝时间，
     * 等待 restartTimeout 后才可正常展示 Toast，
     * 在 restartTimeout 内，再次触发拒绝将重置时间
     */
    fun show(context: Context) {
        if (lastText == nextText || System.currentTimeMillis() - refuseTime < restartTimeout) {
            refuseTime = System.currentTimeMillis()
            return
        }
        lastText = nextText
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, nextText, Toast.LENGTH_SHORT).show()
        }
    }
}
package com.lalilu.lmusic.utils.scanner

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * MScanner接口的基础实现，仍需要传入onScanForEach函数，
 */
abstract class BaseMScanner : MScanner {
    /**
     * contentResolver 所需参数
     */
    open var selection: String? = null
    open var projection: Array<String>? = null
    open var selectionArgs: Array<String>? = null
    open var sortOrder: String? = null
    open var progressCount = 0

    /**
     * 扫描开始的入口函数，简单判读是否已经有任务正在执行
     *
     *  在扫描过程中依次触发预设的生命周期函数
     */
    override fun scanStart(context: Context) {
        if (progressCount != 0) {
            onScanFailed?.invoke("扫描正在进行中！")
            return
        }
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val cursor = context.contentResolver.query(
                    EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder
                ) ?: throw NullPointerException("cursor is null")

                onScanStart?.invoke(cursor.count)
                while (cursor.moveToNext()) {
                    onScanForEach?.invoke(context, cursor)
                    onScanProgress?.invoke(++progressCount)
                }

                cursor.close()
                onScanFinish?.invoke(progressCount)
                progressCount = 0
            }
        } catch (e: Exception) {
            onScanFailed?.invoke(e.message)
        }
    }

    override var onScanCancel: ((nowCount: Int) -> Unit)? = null
    override var onScanForEach: ((context: Context, cursor: Cursor) -> Unit)? = null
    override var onScanFailed: ((msg: String?) -> Unit)? = null
    override var onScanStart: ((totalCount: Int) -> Unit)? = null
    override var onScanProgress: ((nowCount: Int) -> Unit)? = null
    override var onScanFinish: ((resultCount: Int) -> Unit)? = null

    override fun setScanCancel(func: (nowCount: Int) -> Unit): MScanner {
        this.onScanCancel = func
        return this
    }

    override fun setScanForEach(func: (context: Context, cursor: Cursor) -> Unit): MScanner {
        this.onScanForEach = func
        return this
    }

    override fun setScanFailed(func: (msg: String?) -> Unit): MScanner {
        this.onScanFailed = func
        return this
    }

    override fun setScanStart(func: (totalCount: Int) -> Unit): MScanner {
        this.onScanStart = func
        return this
    }

    override fun setScanProgress(func: (nowCount: Int) -> Unit): MScanner {
        this.onScanProgress = func
        return this
    }

    override fun setScanFinish(func: (resultCount: Int) -> Unit): MScanner {
        this.onScanFinish = func
        return this
    }
}
package com.lalilu.lmusic

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.lalilu.lmusic.utils.ThreadPoolUtils

/**
 * MediaScanner接口的基础实现，仍需要实现onScanForEach函数，
 * 需从cursor中提取对应所需的Item对象并返回
 *
 */
abstract class BaseMediaScanner<T> : MediaScanner<T> {
    private var mExecutor = ThreadPoolUtils.newCachedThreadPool()

    /**
     * contentResolver 所需参数
     */
    var selection: String? = null
    var projection: Array<String>? = null
    var selectionArgs: Array<String>? = null
    var sortOrder: String? = null

    /**
     * 扫描开始的入口函数，利用线程池判断是否已经有任务正在执行
     *
     *  在扫描过程中依次触发预设的生命周期函数
     */
    override fun scanStart(context: Context) {
        try {
            if (mExecutor.activeCount >= 1) {
                onScanFailed("is already running!")
                return
            }
            mExecutor.execute {
                val cursor = context.contentResolver.query(
                    EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder
                ) ?: throw NullPointerException("cursor is null")

                var progressCount = 0
                onScanStart(cursor.count)
                while (cursor.moveToNext()) {
                    try {
                        val item: T = onScanForEach(cursor)
                        onScanProgress(++progressCount, item)
                    } catch (e: Exception) {
                        onScanFailed(e.message)
                    }
                }
                onScanFinish(progressCount)
                cursor.close()
            }
        } catch (e: Exception) {
            onScanFailed(e.message)
            e.printStackTrace()
        }
    }

    override fun onScanCancel(nowCount: Int) {}

    /**
     * 返回失败提示信息
     */
    override fun onScanFailed(msg: String?) {}

    override fun onScanStart(totalCount: Int) {}

    /**
     * 返回扫描到的Item和当前扫描到的数量
     */
    override fun onScanProgress(nowCount: Int, item: T) {}

    /**
     * 扫描成功返回获得的结果数量
     */
    override fun onScanFinish(resultCount: Int) {}

    /**
     * 从cursor中提取所需的对象并返回
     */
    abstract override fun onScanForEach(cursor: Cursor): T
}
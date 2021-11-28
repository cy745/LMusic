package com.lalilu.lmusic.utils.scanner

import android.content.Context
import android.database.Cursor

/**
 * 媒体扫描器基类
 *
 */
@Deprecated("MScanner 替代，后期删除")
interface MediaScanner<T> {

    /**
     * 扫描结果回调
     */
    abstract class OnScanCallback<T> {
        open fun onScanStart(totalCount: Int) {}
        open fun onScanFinish(totalCount: Int) {}
        open fun onScanProgress(nowCount: Int, item: T) {}
        open fun onScanException(msg: String?) {}
    }

    /**
     *  扫描功能
     *
     */
    fun scanStart(context: Context)

    /**
     * 扫描循环取消时执行
     */
    fun onScanCancel(nowCount: Int)

    /**
     * 扫描循环中得到的cursor
     */
    fun onScanForEach(cursor: Cursor): T

    /**
     * 扫描任务失败时返回提示信息
     *
     */
    fun onScanFailed(msg: String?)

    /**
     * 开始扫描时获取任务总量
     *
     */
    fun onScanStart(totalCount: Int)

    /**
     * 扫描进行中时获取当前任务的序数
     *
     */
    fun onScanProgress(nowCount: Int, item: T)

    /**
     * 扫描完成后获取总共扫描得到的条目数量
     *
     */
    fun onScanFinish(resultCount: Int)
}
package com.lalilu.lmusic.scanner

import android.content.Context
import android.database.Cursor

/**
 * 媒体扫描器基础接口
 *
 */
interface MScanner {

    /**
     *  扫描功能
     *
     */
    fun scanStart(context: Context)

    /**
     * 扫描循环取消时执行
     */
    var onScanCancel: ((nowCount: Int) -> Unit)?
    fun setScanCancel(func: (nowCount: Int) -> Unit): MScanner

    /**
     * 扫描循环中得到的cursor
     */
    fun onScanForEach(context: Context, cursor: Cursor)

    /**
     * 扫描任务失败时返回提示信息
     */
    var onScanFailed: ((msg: String?) -> Unit)?
    fun setScanFailed(func: (msg: String?) -> Unit): MScanner

    /**
     * 开始扫描时获取任务总量
     */
    var onScanStart: ((totalCount: Int) -> Unit)?
    fun setScanStart(func: (totalCount: Int) -> Unit): MScanner

    /**
     * 扫描进行中时获取当前任务的序数
     */
    var onScanProgress: ((nowCount: Int) -> Unit)?
    fun setScanProgress(func: (nowCount: Int) -> Unit): MScanner

    /**
     * 扫描完成后获取总共扫描得到的条目数量
     */
    var onScanFinish: ((resultCount: Int) -> Unit)?
    fun setScanFinish(func: (resultCount: Int) -> Unit): MScanner
}
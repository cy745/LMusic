package com.lalilu.lmedia.extension

/**
 * 用于为搜索功能存储全文匹配索引的基类
 */
interface Searchable {
    /**
     * 获取匹配的源文本
     */
    fun getMatchSource(): String

    /**
     * 获取实际进行匹配的转换后的文本
     */
    fun getMatchStr(): String {
        return SearchTextManager.createPatternString(getMatchSource())
    }
}
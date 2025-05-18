package com.lalilu.lmedia.entity

import com.lalilu.lmedia.extension.Searchable
import com.lalilu.lmedia.extension.sortable.Sortable

/**
 * 基础元素
 */
interface Item : Sortable, Searchable {
    val id: String
    val name: String
    var blocked: Boolean

    fun <T : Item> link(item: T) {}

    override fun getMatchSource(): String = name

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun <T : Any> getValueBy(key: String): T? {
        return when (key) {
            Sortable.COMPARE_KEY_ID -> id
            Sortable.COMPARE_KEY_TITLE -> name
            else -> super.getValueBy<T>(key)
        } as? T?
    }
}

/**
 * 存有基础元素的组类
 */
interface MusicParent : Item {
    val songs: List<LSong>

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun <T : Any> getValueBy(key: String): T? {
        return when (key) {
            Sortable.COMPARE_KEY_ITEMS_COUNT -> songs.size.toLong()
            Sortable.COMPARE_KEY_DURATION -> songs.fold(0L) { acc, item -> acc + item.metadata.duration }
            Sortable.COMPARE_KEY_FILE_SIZE -> songs.fold(0L) { acc, item -> acc + item.fileInfo.size }
            else -> super.getValueBy<T>(key)
        } as? T?
    }
}

internal inline fun <reified T : MusicParent> List<T>.link() = onEach { parent ->
    parent.songs.onEach { it.link(parent) }
}

/**
 * 根据子项判断自身是否应该被标记为已屏蔽
 */
fun <T : MusicParent> T.isChildrenAllBlocked(): Boolean {
    return songs.all { it.blocked }
}

fun <T : MusicParent> Collection<T>.markBlockedByChildren() {
    onEach { it.blocked = it.blocked || it.isChildrenAllBlocked() }
}

fun <T : Item> Collection<T>.resetBlockState() {
    onEach { it.blocked = false }
}
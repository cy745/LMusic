package com.lalilu.extension_core

import com.lalilu.common.base.Playable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * 宿主端需要随时能获取到插件端的内容，
 * 并且插件端需要能主动更新自己提供的内容，所以使用Flow进行串联
 */
@OptIn(ExperimentalCoroutinesApi::class)
interface Provider {

    /**
     * 用于外部判断是否此Provider是否适用于该传入的ID
     */
    fun isSupported(mediaId: String): Boolean

    /**
     * 传入Id，获取指定的Playable
     */
    fun getById(mediaId: String): Playable?

    /**
     * 传入Id，获取指定的Playable
     */
    fun getFlowById(mediaId: String): Flow<Playable?>

    /**
     * 传入一系列Id，获取List<Playable>
     *
     * NOTE: 可重写以简化获取List的逻辑
     */
    fun getFlowByIds(mediaIds: List<String>): Flow<List<Playable>> {
        val flowList = mediaIds.map { getFlowById(it) }
        return flowOf(flowList)
            .flatMapLatest { list -> combine(list) { songs -> songs.mapNotNull { it } } }
    }
}
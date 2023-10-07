package com.lalilu.extension_core

import androidx.annotation.Keep
import com.lalilu.common.base.Playable
import kotlinx.coroutines.flow.Flow

/**
 * @TODO 待确定，非最终结构
 */
interface Provider {

    fun isSupported(mediaId: String): Boolean

    /**
     * 插件端若需要进行播放，需要将自己的内容提供给宿主端，
     * 宿主端需要能通过mediaId获取到插件提供的内容，以便进行播放
     *
     * @param mediaId String 由插件端定义并提供的媒体内容的ID，
     *
     * NOTE: 需要确保该ID全局唯一，否则可能造成播放内容混乱
     */
    @Keep
    fun getPlayableByMediaId(mediaId: String): Playable?

    fun getPlayableFlowByMediaId(mediaId: String): Flow<Playable?>

    fun getPlayableFlowByMediaIds(mediaIds: List<String>): Flow<List<Playable>>
}
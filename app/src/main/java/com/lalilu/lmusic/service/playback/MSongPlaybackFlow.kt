package com.lalilu.lmusic.service.playback

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.utils.Mathf
import com.lalilu.lmusic.utils.ToastUtil
import com.lalilu.lmusic.utils.toSimpleMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class MSongPlaybackFlow @Inject constructor(
    @ApplicationContext val context: Context,
    dataModule: DataModule,
    override var mAudioFocusManager: LMusicAudioFocusManager,
    override val mToastUtil: ToastUtil
) : FlowPlayback<MSong, List<MSong>, String>(context) {
    override val mediaIdFlow: Flow<String?> = dataModule.mediaId
    override val listFlow: Flow<List<MSong>> = dataModule.nowListFlow
    override val repeatModeFlow: Flow<Int> = dataModule.repeatModeFlow
    override var onPlayerCallback: Playback.OnPlayerCallback? = null

    override val playing: Flow<MSong?> =
        mediaIdFlow.combine(listFlow) { id, list -> getItemFromListByID(list, id) }
    override val previous: Flow<MSong?> =
        mediaIdFlow.combine(listFlow) { id, list -> getPreviousItemFromListByNowID(list, id) }
    override val next: Flow<MSong?> =
        mediaIdFlow.combine(listFlow) { id, list -> getNextItemFromListByNowID(list, id) }

    init {
        mAudioFocusManager.onAudioFocusChangeListener = this
    }

    override fun getPreviousItemFromListByNowID(
        list: List<MSong>,
        id: String?
    ): MSong? {
        val index = list.indexOfFirst { it.songId.toString() == id }
        if (index < 0) return null

        val previous = Mathf.clampInLoop(0, list.size - 1, index - 1)
        return list[previous]
    }

    override fun getNextItemFromListByNowID(list: List<MSong>, id: String?): MSong? {
        val index = list.indexOfFirst { it.songId.toString() == id }
        if (index < 0) return null

        val next = Mathf.clampInLoop(0, list.size - 1, index + 1)
        return list[next]
    }

    override fun getItemFromListByID(list: List<MSong>, id: String?): MSong? {
        val index = list.indexOfFirst { it.songId.toString() == id }
        return if (index < 0) null else list[index]
    }

    override fun getUriFromItem(item: MSong): Uri {
        return item.songUri
    }

    override fun getMetaDataFromItem(item: MSong): MediaMetadataCompat {
        return item.toSimpleMetadata(context)
    }
}
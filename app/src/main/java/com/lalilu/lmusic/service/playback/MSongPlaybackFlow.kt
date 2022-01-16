package com.lalilu.lmusic.service.playback

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.utils.Mathf
import com.lalilu.lmusic.utils.toFullMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class MSongPlaybackFlow @Inject constructor(
    @ApplicationContext context: Context,
    dataModule: DataModule,
    override var mAudioFocusManager: LMusicAudioFocusManager
) : FlowPlayback<FullSongInfo, List<FullSongInfo>, String>(context) {
    override val mediaIdFlow: Flow<String?> = dataModule.mediaId
    override val listFlow: Flow<List<FullSongInfo>> = dataModule.nowListFlow
    override val repeatModeFlow: Flow<Int> = dataModule.repeatModeFlow
    override var onPlayerCallback: Playback.OnPlayerCallback? = null

    override val playing: Flow<FullSongInfo?> =
        mediaIdFlow.combine(listFlow) { id, list -> getItemFromListByID(list, id) }
    override val previous: Flow<FullSongInfo?> =
        mediaIdFlow.combine(listFlow) { id, list -> getPreviousItemFromListByNowID(list, id) }
    override val next: Flow<FullSongInfo?> =
        mediaIdFlow.combine(listFlow) { id, list -> getNextItemFromListByNowID(list, id) }

    init {
        mAudioFocusManager.onAudioFocusChangeListener = this
    }

    override fun getPreviousItemFromListByNowID(
        list: List<FullSongInfo>,
        id: String?
    ): FullSongInfo? {
        val index = list.indexOfFirst { it.song.songId.toString() == id }
        if (index < 0) return null

        val previous = Mathf.clampInLoop(0, list.size - 1, index - 1)
        return list[previous]
    }

    override fun getNextItemFromListByNowID(list: List<FullSongInfo>, id: String?): FullSongInfo? {
        val index = list.indexOfFirst { it.song.songId.toString() == id }
        if (index < 0) return null

        val next = Mathf.clampInLoop(0, list.size - 1, index + 1)
        return list[next]
    }

    override fun getItemFromListByID(list: List<FullSongInfo>, id: String?): FullSongInfo? {
        val index = list.indexOfFirst { it.song.songId.toString() == id }
        return if (index < 0) null else list[index]
    }

    override fun getUriFromItem(item: FullSongInfo): Uri {
        return item.song.songUri
    }

    override fun getMetaDataFromItem(item: FullSongInfo): MediaMetadataCompat {
        return item.song.toFullMetadata(item.detail)
    }
}
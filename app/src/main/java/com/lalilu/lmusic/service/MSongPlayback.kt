package com.lalilu.lmusic.service

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@ExperimentalCoroutinesApi
class MSongPlayback @Inject constructor(
    @ApplicationContext mContext: Context,
    dataModule: DataModule,
    override var mAudioFocusManager: LMusicAudioFocusManager,
) : BasePlayback<MediaMetadataCompat, List<MediaMetadataCompat>, String>(mContext) {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    override var nowPlaylist: Flow<List<MediaMetadataCompat>> =
        dataModule.nowPlaylistMetadataFlow

    init {
        nowPlaying.observeForever {
            println("observeForever: ${it?.description?.title}")
        }
    }

    override fun getUriFromNowItem(nowPlaying: MediaMetadataCompat?): Uri? =
        nowPlaying?.description?.mediaUri

    override fun getIdFromItem(item: MediaMetadataCompat): String = item.description.mediaId ?: "0"

    override fun getMetaDataFromItem(item: MediaMetadataCompat): MediaMetadataCompat = item

    override fun getItemById(
        list: List<MediaMetadataCompat>,
        mediaId: String
    ): MediaMetadataCompat? =
        list.find { it.description.mediaId == mediaId }

    override fun getSizeFromList(list: List<MediaMetadataCompat>): Int = list.size

    override fun getIndexOfFromList(
        list: List<MediaMetadataCompat>,
        item: MediaMetadataCompat
    ): Int =
        list.indexOfFirst { item.description.mediaId == it.description.mediaId }

    override fun getItemFromListByIndex(
        list: List<MediaMetadataCompat>,
        index: Int
    ): MediaMetadataCompat {
        return list[index]
    }
}
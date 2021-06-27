package com.lalilu.lmusic

import android.app.Application
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.LMusicPlayList
import com.lalilu.player.LMusicPlayerModule
import org.jetbrains.annotations.Nullable
import java.util.*

class LMusicPlayListManager private constructor(application: Application) {
    private val mViewModel = LMusicViewModel.getInstance(application)

    fun createPlayList() {
        mViewModel.mPlayListRecyclerView.value?.smoothScrollToPosition(0)
        mViewModel.mViewPager2.value?.currentItem = 1
        mViewModel.mAppBar.value?.setExpanded(true, true)

        val playerModule = LMusicPlayerModule.getInstance(null)
        val iconUri = playerModule.metadata.value?.description?.iconUri
        val title = playerModule.metadata.value?.description?.title
        val playList = TreeSet(playerModule.mediaList.value?.map {
            it.description.title.toString()
        })

        LMusicMediaModule.getInstance(null).database.playlistDao()
            .insert(LMusicPlayList().also {
                it.playListId = 0
                it.playListTitle = "歌单: $title"
                it.playListArtUri = iconUri
                it.mediaIdList = playList
            })
    }

    companion object {
        @Volatile
        private var instance: LMusicPlayListManager? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): LMusicPlayListManager {
            instance ?: synchronized(LMusicPlayListManager::class.java) {
                if (application == null) throw NullPointerException("No Application Context Input")
                instance = LMusicPlayListManager(application)
            }
            return instance!!
        }
    }
}
package com.lalilu.lmusic

import android.app.Application
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Playlist
import com.lalilu.player.LMusicPlayerModule
import org.jetbrains.annotations.Nullable

class LMusicPlayListManager private constructor(application: Application) {
    private val mViewModel = LMusicViewModel.getInstance(application)
    private val mediaModule = LMusicMediaModule.getInstance(application)
    private val playerModule = LMusicPlayerModule.getInstance(application)

    fun createPlayList() {
        mViewModel.mPlayListRecyclerView.value?.smoothScrollToPosition(0)
        mViewModel.mViewPager2.value?.currentItem = 1
        mViewModel.mAppBar.value?.setExpanded(true, true)

        val iconUri = playerModule.metadata.value?.description?.iconUri
        val title = playerModule.metadata.value?.description?.title.toString()

        println(playerModule.mediaList.value)
        mediaModule.database.playListDao().insertPlaylistByList(
            Playlist(1, iconUri, title),
            playerModule.mediaList.value
        )
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
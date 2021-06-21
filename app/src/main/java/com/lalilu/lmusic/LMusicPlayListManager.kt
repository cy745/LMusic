package com.lalilu.lmusic

import android.app.Application
import com.lalilu.lmusic.fragment.LMusicViewModel
import org.jetbrains.annotations.Nullable

class LMusicPlayListManager private constructor(application: Application) {
    private val mViewModel = LMusicViewModel.getInstance(application)

    fun createPlayList() {
        mViewModel.mPlayListRecyclerView.value?.smoothScrollToPosition(0)
        mViewModel.mViewPager2.value?.currentItem = 1
        mViewModel.mAppBar.value?.setExpanded(true, true)

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
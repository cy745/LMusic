package com.lalilu.lmusic.fragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.lalilu.lmusic.adapter2.LMusicNowPlayingAdapter
import com.lalilu.lmusic.adapter2.LMusicPlayListAdapter
import com.lalilu.lmusic.ui.AntiMisOperationRecyclerView
import com.lalilu.lmusic.ui.SquareAppBarLayout
import org.jetbrains.annotations.Nullable

class LMusicViewModel private constructor(application: Application) :
    AndroidViewModel(application) {

    val mAppBar = MutableLiveData<SquareAppBarLayout>(null)
    val mViewPager2 = MutableLiveData<ViewPager2>(null)
    val mNowPlayingRecyclerView = MutableLiveData<AntiMisOperationRecyclerView>(null)
    val mNowPlayingAdapter = MutableLiveData<LMusicNowPlayingAdapter>(null)
    val mPlayListRecyclerView = MutableLiveData<AntiMisOperationRecyclerView>(null)
    val mPlayListAdapter = MutableLiveData<LMusicPlayListAdapter>(null)

    companion object {
        @Volatile
        private var instance: LMusicViewModel? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): LMusicViewModel {
            instance ?: synchronized(LMusicViewModel::class.java) {
                if (application == null) throw NullPointerException("No Application Context Input")
                instance = LMusicViewModel(application)
            }
            return instance!!
        }
    }
}
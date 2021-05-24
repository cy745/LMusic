package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lalilu.lmusic.entity.Song

class MusicServiceViewModel private constructor() : ViewModel() {
    private var playingSong: MutableLiveData<Song> = MutableLiveData()
    private var songList: MutableLiveData<List<Song>> = MutableLiveData()
    private var playingDuration: MutableLiveData<Long> = MutableLiveData(0L)
    private var showingDuration: MutableLiveData<Long> = MutableLiveData(0L)

    fun getPlayingSong(): MutableLiveData<Song> = playingSong
    fun getSongList(): MutableLiveData<List<Song>> = songList
    fun getPlayingDuration(): MutableLiveData<Long> = playingDuration
    fun getShowingDuration(): MutableLiveData<Long> = showingDuration

    companion object {
        @Volatile
        private var mInstance: MusicServiceViewModel? = null

        fun getInstance(): MusicServiceViewModel {
            if (mInstance == null) synchronized(MusicServiceViewModel::class.java) {
                if (mInstance == null) mInstance = MusicServiceViewModel()
            }
            return mInstance!!
        }
    }
}
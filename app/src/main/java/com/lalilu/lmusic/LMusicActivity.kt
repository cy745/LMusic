package com.lalilu.lmusic

import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.databinding.ActivityNowBinding
import com.lalilu.lmusic.base.BaseActivity
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.MainActivityViewModel
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music
import com.lalilu.media.entity.Playlist
import com.lalilu.media.scanner.MediaScanner

class LMusicActivity : BaseActivity() {
    private lateinit var mState: MainActivityViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var playerModule: LMusicPlayerModule
    private lateinit var mediaModule: LMusicMediaModule

    override fun initViewModel() {
        mState = getActivityViewModel(MainActivityViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_now, BR.vm, mState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaModule = LMusicMediaModule.getInstance(application)
        playerModule = LMusicPlayerModule.getInstance(application)
        playerModule.initMusicBrowser(this)
        PermissionUtils.requestPermission(this)

        val seekBar = (mBinding as ActivityNowBinding).maSeekBar
        playerModule.metadata.observe(this) {
            if (it == null) return@observe
            val sum = it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            seekBar.setSumDuration(sum)
        }
        playerModule.playBackState.observe(this) {
            if (it == null) return@observe
            seekBar.updateNowPosition(it)
        }
        seekBar.onActionUp = {
            playerModule.mediaController.value?.transportControls?.seekTo(it)
        }
        mEvent.nowBgPalette.observe(this) {
            mState.nowBgPalette.postValue(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                playerModule.mediaController.value?.transportControls?.play()
            }
            R.id.appbar_pause -> {
                playerModule.mediaController.value?.transportControls?.pause()
            }
            R.id.appbar_create_playlist -> {
                val iconUri = mEvent.nowPlayingMusic.value?.musicArtUri
                val title = "歌单：" + mEvent.nowPlayingMusic.value?.musicTitle

                val musics = mEvent.nowPlaylistRequest.getData().value
                    ?: return super.onOptionsItemSelected(item)

                mediaModule.database.playListDao().insertPlaylistByList(
                    Playlist(playlistArt = iconUri, playlistTitle = title),
                    ArrayList(musics.map { it.musicId })
                )
                mEvent.allPlaylistRequest.requestData()
            }
            R.id.appbar_scan_song -> {
                mediaModule.mediaScanner.setOnScanCallback(object :
                    MediaScanner.OnScanCallback<Music>() {
                    override fun onScanFinish(totalCount: Int) {
                        println("scan done, sum: $totalCount")
                        mEvent.allPlaylistRequest.requestData()
                        mEvent.nowPlaylistRequest.requestData()

                    }
                }).scanStart(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        playerModule.connect()
        super.onStart()
    }

    override fun onStop() {
        playerModule.disconnect()
        super.onStop()
    }

    override fun onResume() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
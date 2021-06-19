package com.lalilu.lmusic

import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.common.getAutomaticColor
import com.lalilu.databinding.ActivityMainBinding
import com.lalilu.lmusic.fragment.LMusicFragmentStateAdapter
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.lmusic.fragment.NowPlayListFragment
import com.lalilu.lmusic.fragment.PlayingFragment
import com.lalilu.lmusic.service2.MusicBrowser
import com.lalilu.lmusic.utils.*
import com.lalilu.media.LMusicMediaModule


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mViewModel: LMusicViewModel
    private lateinit var mediaBrowser: MusicBrowser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.requestPermission(this)
        mediaBrowser = MusicBrowser(this)
        mViewModel = LMusicViewModel.getInstance(application)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager()
        initToolBar()
        initSeekBar()
        bindUiToBrowser()
        initTabLayout()
    }

    private fun initTabLayout() {
        binding.tabLayout.bindToViewPager(binding.musicViewPager)
    }

    private fun initViewPager() {
        val musicViewPager = binding.musicViewPager
        musicViewPager.adapter = LMusicFragmentStateAdapter(this)
        val adapter = musicViewPager.adapter as LMusicFragmentStateAdapter
        adapter.addFragment(NowPlayListFragment())
        adapter.addFragment(PlayingFragment())

        val child = musicViewPager.getChildAt(0) as View
        if (child is RecyclerView) child.overScrollMode = View.OVER_SCROLL_NEVER
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolbar)
        ColorAnimator.setContentScrimColorFromPaletteDraweeView(
            binding.playingSongAlbumPic,
            binding.collapsingToolbarLayout
        )
//        binding.toolbar.setOnClickListener {
//            binding.musicViewPager.smoothScrollToPosition(0)
//            binding.appbar.setExpanded(true, true)
//        }
    }

    private fun initSeekBar() {
        ColorAnimator.getPaletteFromFromPaletteDraweeView(binding.playingSongAlbumPic) {
            binding.seekBar.setThumbColor(it.getAutomaticColor())
        }
        binding.seekBar.onActionUp = {
            mediaBrowser.mediaController.transportControls?.seekTo(it)
        }
        binding.seekBar.setOnClickListener {
            mediaBrowser.mediaController.transportControls.sendCustomAction(
                PlaybackStateCompat.ACTION_PLAY_PAUSE.toString(), null
            )
        }
    }

    private fun bindUiToBrowser() {
        mViewModel.metadata.observeForever {
            if (it == null) return@observeForever
            binding.collapsingToolbarLayout.title = it.description.title
            binding.playingSongAlbumPic.setImageURI(
                it.description.iconUri, this
            )
            binding.seekBar.setSumDuration(
                it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
        }
        mViewModel.playBackState.observeForever {
            if (it == null) return@observeForever
            binding.seekBar.updateNowPosition(it)
        }
    }

    override fun onStart() {
        super.onStart()
        println("[onStart]")
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        mediaBrowser.disconnect()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                mediaBrowser.mediaController.transportControls.play()
            }
            R.id.appbar_pause -> {
                mediaBrowser.mediaController.transportControls.pause()
            }
            R.id.appbar_scan_song -> {
                Thread {
                    mediaBrowser.disconnect()
                    LMusicMediaModule.getInstance(null).mediaScanner.updateSongDataBase {
                        mediaBrowser.connect()
                    }
                }.start()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PermissionUtils.returnPermissionCheck(this, requestCode)
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
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
import androidx.viewpager2.widget.ViewPager2
import com.lalilu.R
import com.lalilu.common.Mathf
import com.lalilu.common.getAutomaticColor
import com.lalilu.databinding.ActivityMainBinding
import com.lalilu.lmusic.fragment.LMusicFragmentStateAdapter
import com.lalilu.lmusic.fragment.LMusicNowPlayingFragment
import com.lalilu.lmusic.fragment.LMusicPlayListFragment
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.lmusic.utils.*
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.LMusicMedia
import com.lalilu.media.scanner.LMusicMediaScanner
import com.lalilu.media.scanner.MediaScanner
import com.lalilu.player.LMusicPlayerModule
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mViewModel: LMusicViewModel
    private lateinit var playerModule: LMusicPlayerModule
    private lateinit var mediaScanner: LMusicMediaScanner
    private lateinit var playlistManager: LMusicPlayListManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.requestPermission(this)
        playerModule = LMusicPlayerModule.getInstance(application)
        playerModule.initMusicBrowser(this)
        playlistManager = LMusicPlayListManager.getInstance(application)
        mViewModel = LMusicViewModel.getInstance(application)
        mediaScanner = LMusicMediaModule.getInstance(application).mediaScanner
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initMediaScanner()
        initViewPager()
        initToolBar()
        initSeekBar()
        initTabLayout()
        bindUiToBrowser()
    }

    private fun initMediaScanner() {
        mediaScanner.setOnScanCallback(object :
            MediaScanner.OnScanCallback<LMusicMedia> {
            var total = 0
            override fun onScanStart(totalCount: Int) {
                total = totalCount
            }

            override fun onScanFinish(totalCount: Int) {
                playerModule.disconnect()
                playerModule.connect()
                binding.collapsingToolbarLayout.title = "扫描完成：共${totalCount}首"
            }

            override fun onScanProgress(nowCount: Int, item: LMusicMedia) {
                binding.collapsingToolbarLayout.title = "扫描中：${nowCount}/${total}"
            }
        })
    }

    private fun initTabLayout() {
        binding.tabLayout.bindToViewPager(binding.musicViewPager)
    }

    private fun initViewPager() {
        binding.musicViewPager.adapter = LMusicFragmentStateAdapter(this)
        binding.musicViewPager.offscreenPageLimit = 1
        binding.musicViewPager.setPageTransformer { page, position ->
            page.alpha = Mathf.clamp(0f, 1f, 1 - abs(position))
        }
        val adapter = binding.musicViewPager.adapter as LMusicFragmentStateAdapter
        adapter.addFragment(LMusicNowPlayingFragment())
        adapter.addFragment(LMusicPlayListFragment())

        val child = binding.musicViewPager.getChildAt(0) as View
        if (child is RecyclerView) child.overScrollMode = View.OVER_SCROLL_NEVER

        binding.musicViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val mAppBarState = mViewModel.mAppBar.value?.mAppbarState ?: return
                val recyclerView = when (position) {
                    0 -> mViewModel.mNowPlayingRecyclerView.value
                    1 -> mViewModel.mPlayListRecyclerView.value
                    else -> null
                } ?: return
                if (recyclerView.totalScrollY > 0 && mAppBarState == AppBarOnStateChange.AppBarState.STATE_EXPANDED) {
                    mViewModel.mAppBar.value?.setExpanded(false, true)
                }
            }
        })
        mViewModel.mViewPager2.postValue(binding.musicViewPager)
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolbar)
        ColorAnimator.setBackgroundColorFromPalette(
            binding.playingSongAlbumPic,
            binding.mAppBarLayout
        )
        binding.toolbar.setOnClickListener {
            if (binding.mAppBarLayout.mAppbarState == AppBarOnStateChange.AppBarState.STATE_COLLAPSED) {
                val recyclerView = when (binding.musicViewPager.currentItem) {
                    0 -> mViewModel.mNowPlayingRecyclerView.value
                    1 -> mViewModel.mPlayListRecyclerView.value
                    else -> null
                } ?: return@setOnClickListener
                if (recyclerView.totalScrollY > 0) {
                    binding.mAppBarLayout.setExpanded(true, true)
                    recyclerView.smoothScrollToPosition(0)
                }
            }
        }
        mViewModel.mAppBar.postValue(binding.mAppBarLayout)
    }

    private fun initSeekBar() {
        ColorAnimator.getPaletteFromFromPaletteDraweeView(binding.playingSongAlbumPic) {
            binding.seekBar.setThumbColor(it.getAutomaticColor())
        }
        binding.seekBar.onActionUp = {
            playerModule.mediaController.value?.transportControls?.seekTo(it)
        }
        binding.seekBar.setOnClickListener {
            playerModule.mediaController.value?.transportControls?.sendCustomAction(
                PlaybackStateCompat.ACTION_PLAY_PAUSE.toString(), null
            )
        }
    }

    private fun bindUiToBrowser() {
        playerModule.metadata.observeForever {
            if (it == null) return@observeForever
            binding.collapsingToolbarLayout.title = it.description.title
            binding.playingSongAlbumPic.setImageURI(
                it.description.iconUri, this
            )
            binding.seekBar.setSumDuration(
                it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
        }
        playerModule.playBackState.observeForever {
            if (it == null) return@observeForever
            binding.seekBar.updateNowPosition(it)
        }
    }

    override fun onStart() {
        super.onStart()
        println("[onStart]")
        playerModule.connect()
    }

    override fun onStop() {
        super.onStop()
        playerModule.disconnect()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                playerModule.mediaController.value?.transportControls?.play()
            }
            R.id.appbar_pause -> {
                playerModule.mediaController.value?.transportControls?.pause()
            }
            R.id.appbar_create_playlist -> playlistManager.createPlayList()
            R.id.appbar_scan_song -> mediaScanner.scanStart(this)
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
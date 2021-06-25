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
import com.lalilu.player.LMusicPlayerModule
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mViewModel: LMusicViewModel
    private lateinit var playerModule: LMusicPlayerModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        PermissionUtils.requestPermission(this)
        playerModule = LMusicPlayerModule.getInstance(application)
        playerModule.initMusicBrowser(this)

        mViewModel = LMusicViewModel.getInstance(application)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

//        val playlist = mutableListOf<String>().apply {
//            this.add("东瀛 1980s 都市流行时光机丨City Pop")
//            this.add("孤零的岛屿终会找到海，你也会遇到对的人")
//            this.add("《動く、動く》和更多好听的|私人雷达")
//            this.add("轻柔女声‖日系治愈 悲伤时请仰望星空")
//            this.add("[日系私人订制] 最懂你的日系推荐 每日更新35首")
//            this.add("「日系」中毒|听说你也喜欢开口跪？")
//            this.add("未来有你・初音未来2018中国巡演")
//            this.add("经典电影里令人印象深刻的旋律")
//            this.add("听你爱的《BPM15Q!》|时光雷达")
//            this.add("初音ミク,DECO*27和更多好听的|乐迷雷达")
//            this.add("泠鸢yousa新歌快递请查收|新歌雷达")
//            this.add("《时光代理人》音乐集")
//            this.add("Kawaii Bass | 谁还不是个小可爱了")
//            this.add("『日系/萝莉音』一颗甜蜜の软糖")
//            this.add("TV动画《薇薇 -萤石眼之歌- 》原声带vivy")
//            this.add("花 影 海 夜 夏 | 荒岛自制卡带®")
//            this.add("あなたの膵臓を食べたい")
//            this.add("很chill的R&B歌单 节奏布鲁斯 欧美")
//        }
//        val dao = LMusicMediaModule.getInstance(application).database.playlistDao()
//
//        playlist.forEach { title ->
//            dao.insert(LMusicPlayList().also {
//                it.playListTitle = title
//                it.mediaIdList.add("未来有你・初音未来2018中国巡演")
//                it.mediaIdList.add("初音ミク")
//                it.mediaIdList.add("很chill的R&B歌单 节奏布鲁斯 欧美")
//                it.mediaIdList.add("花 影 海 夜 夏 | 荒岛自制卡带®")
//            })
//        }

        initViewPager()
        initToolBar()
        initSeekBar()
        initTabLayout()
        bindUiToBrowser()
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
            binding.appbar
        )
        binding.toolbar.setOnClickListener {
            if (binding.appbar.mAppbarState == AppBarOnStateChange.AppBarState.STATE_COLLAPSED) {
                val recyclerView = when (binding.musicViewPager.currentItem) {
                    0 -> mViewModel.mNowPlayingRecyclerView.value
                    1 -> mViewModel.mPlayListRecyclerView.value
                    else -> null
                } ?: return@setOnClickListener
                if (recyclerView.totalScrollY > 0) {
                    binding.appbar.setExpanded(true, true)
                    recyclerView.smoothScrollToPosition(0)
                }
            }
        }
        mViewModel.mAppBar.postValue(binding.appbar)
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
            R.id.appbar_create_playlist -> {
                LMusicPlayListManager.getInstance(null).createPlayList()
            }
            R.id.appbar_scan_song -> {
                Thread {
                    playerModule.disconnect()
                    LMusicMediaModule.getInstance(null).mediaScanner.updateSongDataBase {
                        playerModule.connect()
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
package com.lalilu.lmusic

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lalilu.lmusic.adapter.MusicListAdapter
import com.lalilu.lmusic.databinding.ActivityMainBinding
import com.lalilu.lmusic.service.MusicService
import com.lalilu.lmusic.service.MusicServiceConn
import com.lalilu.lmusic.utils.ColorAnimator
import com.lalilu.lmusic.utils.MediaUtils
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.viewmodel.MusicDataBaseViewModel
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dataBaseViewModel: MusicDataBaseViewModel
    private lateinit var serviceViewModel: MusicServiceViewModel
    private lateinit var musicConn: MusicServiceConn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.requestPermission(this)

        dataBaseViewModel = MusicDataBaseViewModel.getInstance(application)
        serviceViewModel = MusicServiceViewModel.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        musicConn = MusicServiceConn()
        bindService(Intent(this, MusicService::class.java).also {
            startService(it)
        }, musicConn, BIND_AUTO_CREATE)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ColorAnimator.setContentScrimColorFromPaletteDraweeView(
            binding.playingSongAlbumPic,
            binding.collapsingToolbarLayout
        )

        binding.musicRecyclerView.adapter = MusicListAdapter(this)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.toolbar.setOnClickListener {
            binding.musicRecyclerView.smoothScrollToPosition(0)
            binding.appbar.setExpanded(true, true)
        }
        binding.seekBar.setOnActionUp {
            serviceViewModel.getPlayingDuration().postValue(it)
        }

        dataBaseViewModel.getSongsLiveDate().observeForever {
            (binding.musicRecyclerView.adapter as MusicListAdapter).setSongList(it)
            MusicServiceViewModel.getInstance().getSongList().postValue(it)
        }

        serviceViewModel.getPlayingSong().observeForever {
            if (it != null) {
                binding.collapsingToolbarLayout.title = it.songTitle
                binding.playingSongAlbumPic.setImageURI(it.albumUri, this)
                binding.seekBar.setSumDuration(it.songDuration)
            }
        }
        serviceViewModel.getShowingDuration().observeForever {
            binding.seekBar.updateDuration(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appbar_play -> {
                Toast.makeText(this, "开始", Toast.LENGTH_SHORT).show()
                musicConn.binder?.play()
            }
            R.id.appbar_pause -> {
                Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show()
                musicConn.binder?.pause()
            }
            R.id.appbar_add_folder -> {
                MediaUtils.updateSongDataBase(this)
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicConn)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
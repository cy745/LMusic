package com.lalilu.lmusic

import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lalilu.lmusic.adapter2.MusicListAdapter
import com.lalilu.lmusic.databinding.ActivityMainBinding
import com.lalilu.lmusic.service.MusicServiceConn
import com.lalilu.lmusic.service2.MusicBrowser
import com.lalilu.lmusic.utils.AudioMediaScanner
import com.lalilu.lmusic.utils.ColorAnimator
import com.lalilu.lmusic.utils.PermissionUtils
import com.lalilu.lmusic.viewmodel.MusicDataBaseViewModel
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dataBaseViewModel: MusicDataBaseViewModel
    private lateinit var serviceViewModel: MusicServiceViewModel
    private lateinit var musicConn: MusicServiceConn

    private lateinit var mediaBrowser: MusicBrowser
    private lateinit var audioMediaScanner: AudioMediaScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.requestPermission(this)

        audioMediaScanner = (application as MusicApplication).audioMediaScanner
        dataBaseViewModel = MusicDataBaseViewModel.getInstance(application)
        serviceViewModel = MusicServiceViewModel.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        mediaBrowser = MusicBrowser(this)

//        musicConn = MusicServiceConn()
//        bindService(Intent(this, MusicService::class.java).also {
//            startService(it)
//        }, musicConn, BIND_AUTO_CREATE)

        setContentView(binding.root)

        initToolBar()
        initRecyclerView()
        initSeekBar()
        bindUiToBrowser()
//        serviceViewModel.getPlayingSong().observeForever {
//            if (it != null) {
//                binding.collapsingToolbarLayout.title = it.songTitle
//                binding.playingSongAlbumPic.setImageURI(
//                    audioMediaScanner.loadThumbnail(it), this
//                )
//                binding.seekBar.setSumDuration(it.songDuration)
//            }
//        }
    }

    private fun initRecyclerView() {
//        binding.musicRecyclerView.adapter = MusicListAdapter(this) {
//            serviceViewModel.getPlayingSong().postValue(it)
//        }
        binding.musicRecyclerView.adapter = MusicListAdapter(this)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this)
//        dataBaseViewModel.getSongsLiveDate().observeForever {
//            if (it != null) {
//                (binding.musicRecyclerView.adapter as MusicListAdapter).setSongList(it)
//                serviceViewModel.getSongList().postValue(it)
//            }
//        }
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolbar)
        ColorAnimator.setContentScrimColorFromPaletteDraweeView(
            binding.playingSongAlbumPic,
            binding.collapsingToolbarLayout
        )
        binding.toolbar.setOnClickListener {
            binding.musicRecyclerView.smoothScrollToPosition(0)
            binding.appbar.setExpanded(true, true)
        }
    }

    private fun initSeekBar() {
        ColorAnimator.getPaletteFromFromPaletteDraweeView(binding.playingSongAlbumPic) {
            binding.seekBar.setThumbColor(it.getDarkVibrantColor(0))
        }
        binding.seekBar.setOnActionUp {
            serviceViewModel.getPlayingDuration().postValue(it)
        }
        binding.seekBar.setOnClickListener {
            mediaBrowser.mediaController.transportControls.sendCustomAction(
                PlaybackStateCompat.ACTION_PLAY_PAUSE.toString(), null
            )
//            musicConn.binder?.toggle()
        }
        mediaBrowser.mediaMetadataCompat.observeForever {
            if (it == null) return@observeForever
            println(
                "[METADATA_KEY_DURATION]" +
                        it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
            binding.seekBar.setSumDuration(
                it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
        }
        mediaBrowser.playbackStateCompat.observeForever {
            if (it == null) return@observeForever
            binding.seekBar.setNewestDuration(it)
        }
//        serviceViewModel.getShowingDuration().observeForever {
//            binding.seekBar.updateDuration(it)
//        }
    }

    private fun bindUiToBrowser() {
        mediaBrowser.mediaMetadataCompat.observeForever {
            if (it == null) return@observeForever
            binding.collapsingToolbarLayout.title = it.description.title
            binding.playingSongAlbumPic.setImageURI(
                it.description.iconUri, this
            )
        }

        mediaBrowser.setAdapterToUpdate(binding.musicRecyclerView.adapter as MusicListAdapter)
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
                Toast.makeText(this, "开始", Toast.LENGTH_SHORT).show()
//                musicConn.binder?.play()
                mediaBrowser.mediaController.transportControls.play()
            }
            R.id.appbar_pause -> {
                Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show()
//                musicConn.binder?.pause()
                mediaBrowser.mediaController.transportControls.pause()
            }
            R.id.appbar_add_folder -> {
                Thread {
                    audioMediaScanner.updateSongDataBase(this)
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

    override fun onDestroy() {
        super.onDestroy()
//        unbindService(musicConn)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
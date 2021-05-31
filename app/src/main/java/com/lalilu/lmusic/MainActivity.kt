package com.lalilu.lmusic

import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lalilu.lmusic.adapter2.MusicListAdapter
import com.lalilu.lmusic.databinding.ActivityMainBinding
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

        setContentView(binding.root)

        initToolBar()
        initRecyclerView()
        initSeekBar()
        bindUiToBrowser()
    }

    private fun initRecyclerView() {
        binding.musicRecyclerView.adapter = MusicListAdapter(this)
        binding.musicRecyclerView.layoutManager = LinearLayoutManager(this)
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
        }
        mediaBrowser.mediaMetadataCompat.observeForever {
            if (it == null) return@observeForever
            binding.seekBar.setSumDuration(
                it.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
        }
        mediaBrowser.playbackStateCompat.observeForever {
            if (it == null) return@observeForever
            binding.seekBar.setNewestDuration(it)
        }
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
                mediaBrowser.mediaController.transportControls.play()
            }
            R.id.appbar_pause -> {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}